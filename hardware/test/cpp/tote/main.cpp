#include "VTote.h"
#include "VTote_SB_SPRAM256KA.h"
#include "VTote_Spram.h"
#include "VTote_Tote.h"
#include "VTote_VexRiscv.h"
#include "testbench.h"

#include <fstream>
#include <functional>
#include <iostream>
#include <sstream>
#include <iomanip>
#include <bitset>

using namespace std;

#define SYSTEM_CLK_HZ 12000000
#define SERIAL_BAUDRATE 115200
#define TIMESCALE uint64_t(1e12)

char* argString(const char *key, int argc, char **argv){
    for(int idx = 0;idx < argc;idx++){
        if(!strcmp(argv[idx], key)){
            return argv[idx + 1];
        }
    }
    return NULL;
}

class VexRiscvTracer : public Agent{
public:
	VTote_VexRiscv *cpu;
  bool trace_instruction;
  bool trace_reg;
	ofstream instructionTraces;
	ofstream regTraces;

	VexRiscvTracer(VTote_VexRiscv *cpu, bool trace_instruction, bool trace_reg){
		this->cpu = cpu;
    this->trace_instruction = trace_instruction;
    this->trace_reg = trace_reg;
    if (this->trace_instruction) {
      instructionTraces.open ("instructionTrace.log");
    }
    if (this->trace_reg) {
      regTraces.open ("regTraces.log");
    }
	}

	virtual void preCycle(uint64_t time){
    if (this->trace_instruction && cpu->lastStageIsFiring){
		 	instructionTraces << " PC " << hex << setw(8) <<  cpu->lastStagePc << " : " << hex << setw(8) <<  cpu->lastStageInstruction << endl;
		}
		
		if(this->trace_reg && cpu->lastStageRegFileWrite_valid == 1 && cpu->lastStageRegFileWrite_payload_address != 0){
		 	regTraces << " PC " << hex << setw(8) <<  cpu->lastStagePc << " : reg[" << dec << setw(2) << (uint32_t)cpu->lastStageRegFileWrite_payload_address << "] = " << hex << setw(8) << cpu->lastStageRegFileWrite_payload_data << endl;
		}

    if (cpu->lastStageIsFiring && cpu->lastStageInstruction == 0x00050013) { //mv	zero,a0
      cout << (char)cpu->RegFilePlugin_regFile[10];
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
  cout << "Simulation start" << endl;
  Verilated::commandArgs(argc, argv);
  TESTBENCH<VTote> *tb =
      new TESTBENCH<VTote>(TIMESCALE / SYSTEM_CLK_HZ);
 
  char *ramBin = argString("--ramBin", argc, argv);
  if (ramBin) {
    assert(access(ramBin, F_OK) != -1);
    FILE *ram_binFile = fopen(ramBin, "r");
    fseek(ram_binFile, 0, SEEK_END);
    uint32_t ram_binSize = ftell(ram_binFile);
    fseek(ram_binFile, 0, SEEK_SET);
    uint8_t *ram_bin = new uint8_t[ram_binSize];
    fread(ram_bin, 1, ram_binSize, ram_binFile);

    uint8_t *ram0 = (uint8_t *)tb->dut->Tote->system_ram->mems_0->mem;
    uint8_t *ram1 = (uint8_t *)tb->dut->Tote->system_ram->mems_1->mem;

    for (int i = 0; i < ram_binSize; i++) {
      switch (i & 3) {
        case 0:
          ram0[i / 4 * 2 + 0] = ram_bin[i];
          break;
        case 1:
          ram0[i / 4 * 2 + 1] = ram_bin[i];
          break;
        case 2:
          ram1[i / 4 * 2 + 0] = ram_bin[i];
          break;
        case 3:
          ram1[i / 4 * 2 + 1] = ram_bin[i];
          break;
      }
    }
  }

  uint64_t timeout = -1;
  char *timeoutStr = argString("--timeout", argc, argv);
  if (timeoutStr) {
    std::istringstream iss(timeoutStr);
    iss >> timeout;
  }

  tb->add(new VexRiscvTracer(tb->dut->Tote->system_cpu, true, true));
  //tb->add(new LedsTracer(&tb->dut->io_leds, false));

  tb->reset();

  while (!tb->done()) {
    tb->tick();
    if (tb->tickCount > timeout) break;
  }
  exit(EXIT_SUCCESS);

  cout << "Simulation end" << endl;
}