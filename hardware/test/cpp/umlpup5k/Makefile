TOPLEVEL = UMlpUp5k
ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJ_TOP_DIR = $(ROOT_DIR)/../../../..

VERILOGS = ${TOPLEVEL}.v
HEADERS  = ../common/testbench.h
SRCS     = main.cpp

TRACE ?= no
DEBUG ?= no

CFLAGS ?= -I../../common -std=c++11
VERILATOR_ARGS ?= --gdbbt -Wno-UNOPTFLAT -Wno-WIDTH --x-assign unique -I${PROJ_TOP_DIR}/hardware/main/resources/rtl/lattice/ice40

ifeq (${TRACE},yes)
	VERILATOR_ARGS += --trace
	CFLAGS += -DTRACE
endif

ifeq (${DEBUG},yes)
	CFLAGS += -O0 -g
else
	CFLAGS += -O3
endif

%.v: ${PROJ_TOP_DIR}/hardware/main/scala/neuralfpga/umlp/${TOPLEVEL}.scala
	cd ${PROJ_TOP_DIR} && sbt "runMain neuralfpga.umlp.${TOPLEVEL} ${ROOT_DIR}"

run: ./obj_dir/V${TOPLEVEL}
	./obj_dir/V${TOPLEVEL} ${ARGS}

./obj_dir: ${VERILOGS}
	rm -rf ./obj_dir
	verilator --cc --top-module ${TOPLEVEL} ${VERILOGS} -O3 -CFLAGS "${CFLAGS}" ${VERILATOR_ARGS} --exe ${SRCS}
 	
./obj_dir/V${TOPLEVEL}: ./obj_dir ${SRCS} ${HEADERS}
	make  -j${THREAD_COUNT} -C obj_dir/ -f V${TOPLEVEL}.mk V${TOPLEVEL}
 	
compile: ./obj_dir/V${TOPLEVEL}

clean:
	rm -rf obj_dir
	rm -f ${TOPLEVEL}.v
	rm -f ${TOPLEVEL}.v*.bin
	rm -f *.log
	rm -f *.vcd

all: clean compile
