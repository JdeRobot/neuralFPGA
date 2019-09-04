#include "VTote_tb.h"
#include "VTote_tb_Tote_tb.h"
#include "VTote_tb_SB_SPRAM256KA.h"
#include "VTote_tb_Spram.h"
#include "VTote_tb_Tote.h"
#include "VTote_tb_VexRiscv.h"
#include "testbench.h"

#include <bitset>
#include <elfio/elfio_dump.hpp>
#include <fstream>
#include <functional>
#include <iomanip>
#include <iostream>
#include <sstream>

#define SYSTEM_CLK_HZ 12000000
#define SERIAL_BAUDRATE 115200
#define TIMESCALE uint64_t(1e12)

char *argString(const char *key, int argc, char **argv) {
  for (int idx = 0; idx < argc; idx++) {
    if (!strcmp(argv[idx], key)) {
      return argv[idx + 1];
    }
  }
  return NULL;
}

bool argFlag(const char *key, int argc, char **argv) {
  bool flagFound = false;
  for (int idx = 0; idx < argc; idx++) {
    if (!strcmp(argv[idx], key)) {
      flagFound = true;
      break;
    }
  }
  return flagFound;
}

class VexRiscvTracer : public Agent {
 public:
  VTote_tb_VexRiscv *cpu;
  bool trace_instruction;
  bool trace_reg;
  std::ofstream instructionTraces;
  std::ofstream regTraces;

  VexRiscvTracer(VTote_tb_VexRiscv *cpu, bool trace_instruction, bool trace_reg) {
    this->cpu = cpu;
    this->trace_instruction = trace_instruction;
    this->trace_reg = trace_reg;
    if (this->trace_instruction) {
      instructionTraces.open("instructionTrace.log");
    }
    if (this->trace_reg) {
      regTraces.open("regTraces.log");
    }
  }

  virtual void preCycle(uint64_t time) {
    if (this->trace_instruction && cpu->lastStageIsFiring) {
      instructionTraces << " PC " << std::hex << std::setw(8)
                        << cpu->lastStagePc << " : " << std::hex << std::setw(8)
                        << cpu->lastStageInstruction << std::endl;
    }

    if (this->trace_reg && cpu->lastStageRegFileWrite_valid == 1 &&
        cpu->lastStageRegFileWrite_payload_address != 0) {
      regTraces << " PC " << std::hex << std::setw(8) << cpu->lastStagePc
                << " : reg[" << std::dec << std::setw(2)
                << (uint32_t)cpu->lastStageRegFileWrite_payload_address
                << "] = " << std::hex << std::setw(8)
                << cpu->lastStageRegFileWrite_payload_data;
      if (isprint(static_cast<unsigned char>(
              cpu->lastStageRegFileWrite_payload_data))) {
        regTraces << "("
                  << static_cast<unsigned char>(
                         cpu->lastStageRegFileWrite_payload_data)
                  << ")";
      }
      regTraces << std::endl;
    }
  }
};

// class LedsTracer : public Agent{
// public:
// 	CData *io_leds;
//   CData io_leds_prev;
//   bool trace_leds_state;

// 	LedsTracer(CData *io_leds, bool trace_leds_state){
// 		this->io_leds = io_leds;
//     this->io_leds_prev = 0xFF;
//     this->trace_leds_state = trace_leds_state;
// 	}

// 	virtual void preCycle(uint64_t time){
//     if (this->trace_leds_state && *(this->io_leds) != this->io_leds_prev){
//       this->io_leds_prev = *this->io_leds;
//       cout << "LEDS: " << bitset<3>(*this->io_leds) << endl;
// 		}
//   }
// };

int main(int argc, char **argv) {
  Verilated::commandArgs(argc, argv);
  TESTBENCH<VTote_tb> *tb = new TESTBENCH<VTote_tb>(TIMESCALE / SYSTEM_CLK_HZ);

  if (argFlag("--jtag-enabled", argc, argv)) {
    tb->dut->io_enable_jtag = 1;
  }

  uint32_t exit_address = 0;
  char *programPath = argString("--program", argc, argv);
  if (programPath) {
    std::cerr << "Loading program: " << programPath << std::endl;

    ELFIO::elfio reader;
    assert(reader.load(programPath) &&
           "Can't open program file or it is not an ELF file");
    assert(reader.get_machine() == EM_RISCV && "Program is not RiscV");
    assert(reader.get_class() == ELFCLASS32 && "Program is not ELF32");
    assert(reader.get_encoding() == ELFDATA2LSB &&
           "Program is not Little Endian");
    assert(reader.segments.size() > 0 && "Program has no segments to load");

    size_t iram_max_size = sizeof(tb->dut->Tote_tb->tote->system_iRam->mems_0->mem) * 2;
    uint8_t *ram0 = (uint8_t *)tb->dut->Tote_tb->tote->system_iRam->mems_0->mem;
    uint8_t *ram1 = (uint8_t *)tb->dut->Tote_tb->tote->system_iRam->mems_1->mem;

    // use the load address of the first segment as base address
    ELFIO::Elf64_Addr base_addr = reader.segments[0]->get_physical_address();

    for (int i = 0; i < reader.segments.size(); ++i) {
      const ELFIO::segment *pseg = reader.segments[i];
      std::cerr << "LOAD  [" << i << "] 0x" << std::hex << pseg->get_flags()
                << "\t0x" << pseg->get_physical_address() << "\t0x"
                << pseg->get_file_size() << "\t0x" << pseg->get_memory_size()
                << std::endl;
      ELFIO::Elf64_Addr start_addr = pseg->get_physical_address() - base_addr;
      for (int i = 0; i < pseg->get_file_size(); i++) {
        assert((i + start_addr) < iram_max_size &&
               "Program doesn't fit in iRam");
        switch ((i + start_addr) & 3) {
          case 0:
            ram0[(i + start_addr) / 4 * 2 + 0] = pseg->get_data()[i];
            break;
          case 1:
            ram0[(i + start_addr) / 4 * 2 + 1] = pseg->get_data()[i];
            break;
          case 2:
            ram1[(i + start_addr) / 4 * 2 + 0] = pseg->get_data()[i];
            break;
          case 3:
            ram1[(i + start_addr) / 4 * 2 + 1] = pseg->get_data()[i];
            break;
        }
      }
    }

    // find _exit symbol address
    for (int i = 0; i < reader.sections.size(); ++i) {
      ELFIO::section *psec = reader.sections[i];
      // Check section type
      if (psec->get_type() == SHT_SYMTAB) {
        const ELFIO::symbol_section_accessor symbols(reader, psec);
        for (unsigned int j = 0; j < symbols.get_symbols_num(); ++j) {
          std::string name;
          ELFIO::Elf64_Addr value;
          ELFIO::Elf_Xword size;
          unsigned char bind;
          unsigned char type;
          ELFIO::Elf_Half section_index;
          unsigned char other;

          // Read symbol properties
          symbols.get_symbol(j, name, value, size, bind, type, section_index,
                             other);
          if (name == "_exit") {
            std::cerr << "_exit at " << value << std::endl;
            exit_address = static_cast<uint32_t>(value);
          }
        }
      }
    }
  }

  uint64_t timeout = -1;
  char *timeoutStr = argString("--timeout", argc, argv);
  if (timeoutStr) {
    std::istringstream iss(timeoutStr);
    iss >> timeout;
  }

  std::cerr << "Simulation start" << std::endl;

  tb->add(new VexRiscvTracer(
      tb->dut->Tote_tb->tote->system_cpu,
      argFlag("--trace-instructions", argc, argv),
      argFlag("--trace-registers", argc, argv)
    )
  );
  // tb->add(new LedsTracer(&tb->dut->io_leds, false));

  tb->reset();

  int exit_code = EXIT_SUCCESS;
  while (!tb->done()) {
    tb->tick();
    if (tb->tickCount > timeout) break;
    // system_dBus_cmd is firing && is write cmd && address == OUTPORT, see platform_write.h
    if (tb->dut->Tote_tb->tote->system_dBus_cmd_valid &&
        tb->dut->Tote_tb->tote->system_dBus_cmd_ready &&
        tb->dut->Tote_tb->tote->system_dBus_cmd_payload_write &&
        tb->dut->Tote_tb->tote->system_dBus_cmd_payload_address == 0xFFFFFFF8) {
      std::cout << static_cast<char>(tb->dut->Tote_tb->tote->system_dBus_cmd_payload_data);
    }
    if (tb->dut->Tote_tb->tote->system_cpu->lastStageIsFiring &&
        tb->dut->Tote_tb->tote->system_cpu->lastStagePc == exit_address) {
      exit_code = static_cast<int>(tb->dut->Tote_tb->tote->system_cpu->RegFilePlugin_regFile[10]);//a0
      std::cerr << "_exit with code=" << exit_code << std::endl;
      break;
    }
  }
  
  return exit_code;
}