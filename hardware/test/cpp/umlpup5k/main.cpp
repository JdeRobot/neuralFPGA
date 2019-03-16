#include "VUMlpUp5k.h"
#include "VUMlpUp5k_SB_SPRAM256KA.h"
#include "VUMlpUp5k_Bram.h"
#include "VUMlpUp5k_Spram.h"
#include "VUMlpUp5k_UMlpUp5k.h"
#include "VUMlpUp5k_VexRiscv.h"
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
	VUMlpUp5k_VexRiscv *cpu;
  bool trace_instruction;
  bool trace_reg;
	ofstream instructionTraces;
	ofstream regTraces;

	VexRiscvTracer(VUMlpUp5k_VexRiscv *cpu, bool trace_instruction, bool trace_reg){
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
    if (this->trace_instruction && cpu->writeBack_arbitration_isFiring){
		 	instructionTraces << " PC " << hex << setw(8) <<  cpu->writeBack_PC << " : " << hex << setw(8) <<  cpu->writeBack_INSTRUCTION << endl;
		}
		
		if(this->trace_reg && cpu->writeBack_RegFilePlugin_regFileWrite_valid == 1 && cpu->writeBack_RegFilePlugin_regFileWrite_payload_address != 0){
		 	regTraces << " PC " << hex << setw(8) <<  cpu->writeBack_PC << " : reg[" << dec << setw(2) << (uint32_t)cpu->writeBack_RegFilePlugin_regFileWrite_payload_address << "] = " << hex << setw(8) << cpu->writeBack_RegFilePlugin_regFileWrite_payload_data << endl;
		}
  }
};

class LedsTracer : public Agent{
public:
	CData *io_leds;
  CData io_leds_prev;
  bool trace_leds_state;
  bool trace_reg;
	ofstream instructionTraces;
	ofstream regTraces;

	LedsTracer(CData *io_leds, bool trace_leds_state){
		this->io_leds = io_leds;
    this->io_leds_prev = 0xFF;
    this->trace_leds_state = trace_leds_state;
    if (this->trace_leds_state) {
      instructionTraces.open ("ledsTrace.log");
    }
	}

	virtual void preCycle(uint64_t time){
    if (this->trace_leds_state && *(this->io_leds) != this->io_leds_prev){
      this->io_leds_prev = *this->io_leds;
      cout << "LEDS: " << bitset<3>(*this->io_leds) << endl;
		 	//instructionTraces << " PC " << hex << setw(8) <<  cpu->writeBack_PC << " : " << hex << setw(8) <<  cpu->writeBack_INSTRUCTION << endl;
		}
  }
};

int main(int argc, char **argv) {
  cout << "Simulation start" << endl;
  Verilated::commandArgs(argc, argv);
  TESTBENCH<VUMlpUp5k> *tb =
      new TESTBENCH<VUMlpUp5k>(TIMESCALE / SYSTEM_CLK_HZ);
 
  char *iramBin = argString("--iramBin", argc, argv);
  if (iramBin) {
    assert(access(iramBin, F_OK) != -1);
    FILE *ram_binFile = fopen(iramBin, "r");
    fseek(ram_binFile, 0, SEEK_END);
    uint32_t ram_binSize = ftell(ram_binFile);
    fseek(ram_binFile, 0, SEEK_SET);
    uint8_t *ram_bin = new uint8_t[ram_binSize];
    fread(ram_bin, 1, ram_binSize, ram_binFile);

    uint8_t *ram0 = (uint8_t *)tb->dut->UMlpUp5k->system_iRam->mem_symbol0;
    uint8_t *ram1 = (uint8_t *)tb->dut->UMlpUp5k->system_iRam->mem_symbol1;
    uint8_t *ram2 = (uint8_t *)tb->dut->UMlpUp5k->system_iRam->mem_symbol2;
    uint8_t *ram3 = (uint8_t *)tb->dut->UMlpUp5k->system_iRam->mem_symbol3;
    
    for (int i = 0; i < ram_binSize; i++) {
      switch (i & 3) {
        case 0:
          ram0[i / 4] = ram_bin[i];
          break;
        case 1:
          ram1[i / 4] = ram_bin[i];
          break;
        case 2:
          ram2[i / 4] = ram_bin[i];
          break;
        case 3:
          ram3[i / 4] = ram_bin[i];
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

  tb->add(new VexRiscvTracer(tb->dut->UMlpUp5k->system_cpu, true, true));
  tb->add(new LedsTracer(&tb->dut->io_leds, true));

  tb->reset();

  while (!tb->done()) {
    tb->tick();
    if (tb->tickCount > timeout) break;
  }
  exit(EXIT_SUCCESS);

  cout << "Simulation end" << endl;
}