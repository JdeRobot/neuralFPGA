TOPLEVEL = Tote_tb
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJ_TOP_DIR = $(realpath $(ROOT_DIR)/../..)

VERILOGS = ${TOPLEVEL}.v ${ROOT_DIR}/rtl/Tote.v ${PROJ_TOP_DIR}/sim/jtag/SimJTAG.v
HEADERS  = ${PROJ_TOP_DIR}/sim/common/testbench.h
SRCS     = main.cpp ${PROJ_TOP_DIR}/sim/jtag/SimJTAG.cc ${PROJ_TOP_DIR}/sim/jtag/remote_bitbang.cc

TRACE ?= no
DEBUG ?= no

CFLAGS ?= -I${PROJ_TOP_DIR}/sim/common -I${PROJ_TOP_DIR}/libs/ELFIO -std=c++11 -DJTAG_BB_TEST_FRAMEWORK=1
VERILATOR_ARGS ?= --gdbbt -Wno-UNOPTFLAT -Wno-WIDTH --x-assign unique \
	-I${PROJ_TOP_DIR}/hardware/main/resources/rtl/lattice/ice40

ifeq (${TRACE},yes)
	VERILATOR_ARGS += --trace
	CFLAGS += -DTRACE
endif

ifeq (${DEBUG},yes)
	CFLAGS += -O0 -g
else
	CFLAGS += -O3
endif

compile: ./obj_dir/V${TOPLEVEL}

${ROOT_DIR}/rtl/Tote.v: ${PROJ_TOP_DIR}/hardware/main/scala/neuralfpga/core/Tote.scala
	mkdir -p ${ROOT_DIR}/rtl
	cd ${PROJ_TOP_DIR} && sbt "runMain neuralfpga.core.Tote ${ROOT_DIR}/rtl"

run: ./obj_dir/V${TOPLEVEL}
	./obj_dir/V${TOPLEVEL} ${ARGS}

./obj_dir: ${VERILOGS}
	rm -rf ./obj_dir
	verilator --cc --top-module ${TOPLEVEL} ${VERILOGS} -O3 -CFLAGS "${CFLAGS}" ${VERILATOR_ARGS} --exe ${SRCS}
 	
./obj_dir/V${TOPLEVEL}: ./obj_dir ${SRCS} ${HEADERS}
	make  -j${THREAD_COUNT} -C obj_dir/ -f V${TOPLEVEL}.mk V${TOPLEVEL}

clean:
	rm -rf obj_dir
	rm -rf rtl
	rm -f ${TOPLEVEL}.v*.bin
	rm -f *.log
	rm -f *.vcd

all: clean compile