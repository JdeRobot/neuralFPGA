TOPLEVEL := Tote_sim

TRACE ?= no
DEBUG ?= no

RTL_INC := $(foreach dir,$(RTL_DIRS),-I$(dir))
RTL_DEPS := $(foreach dir,$(RTL_DIRS),$(wildcard $(dir)/*.v))
CFLAGS ?= -I$(realpath include) -I$(realpath libs/ELFIO) -std=c++11
VERILATOR_ARGS ?= -Wno-UNOPTFLAT -Wno-WIDTH --x-assign unique $(RTL_INC)

ifeq ($(TRACE),yes)
	VERILATOR_ARGS += --trace
	CFLAGS += -DTRACE
endif

ifeq ($(DEBUG),yes)
	CFLAGS += -O0 -g
else
	CFLAGS += -O3
endif

HEADERS  := include/testbench.h
ifeq ($(JTAG),yes)
	SRCS := src/main.cpp src/SimJTAG.cc src/remote_bitbang.cc
	VERILOGS = rtl/$(TOPLEVEL)_jtag.v rtl/SimJTAG.v
else
	SRCS     := src/main.cpp
	VERILOGS := rtl/$(TOPLEVEL).v
endif

totesim: obj_dir/V$(TOPLEVEL)

obj_dir/V$(TOPLEVEL): $(RTL_DEPS) $(VERILOGS) $(SRCS) $(HEADERS)
	verilator --cc --top-module $(TOPLEVEL) $(VERILOGS) -O3 -CFLAGS "$(CFLAGS)" $(VERILATOR_ARGS) --exe $(SRCS)
	make  -j$(THREAD_COUNT) -C obj_dir/ -f V$(TOPLEVEL).mk V$(TOPLEVEL)

clean:
	rm -rf obj_dir
	rm -f $(TOPLEVEL).v*.bin
	rm -f *.log
	rm -f *.vcd
.PHONY=clean