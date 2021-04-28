TOOLCHAIN_PREFIX ?= riscv64-unknown-elf-
CC=${TOOLCHAIN_PREFIX}gcc
CXX=${TOOLCHAIN_PREFIX}g++
OBJCOPY=${TOOLCHAIN_PREFIX}objcopy
OBJDUMP=${TOOLCHAIN_PREFIX}objdump

MARCH := rv32i
BENCH ?= no

ifeq ($(RV_M),yes)
	MARCH := $(MARCH)m
endif
ifeq ($(RV_C),yes)
	MARCH := $(MARCH)c
endif

ifeq ($(DEBUG),yes)
	CFLAGS += -g3 -Og
endif

ifeq ($(DEBUG),no)
	CFLAGS += -DNDEBUG
ifeq ($(BENCH),no)
	CFLAGS += -Os
else
	CFLAGS += -O3
endif
endif

CFLAGS += -march=$(MARCH) -mabi=ilp32 -mcmodel=medany
CXXFLAGS += -march=$(MARCH) -mabi=ilp32 -mcmodel=medany
