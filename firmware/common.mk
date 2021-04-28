ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
LITEX_BUILD_DIR ?= $(realpath $(ROOT_DIR)/../build/sim)
include $(LITEX_BUILD_DIR)/software/include/generated/variables.mak

TARGET_TOOLCHAIN_PREFIX ?= riscv-none-embed-

CXX             := $(TARGET_TOOLCHAIN_PREFIX)g++
CC              := $(TARGET_TOOLCHAIN_PREFIX)gcc
AS              := $(TARGET_TOOLCHAIN_PREFIX)as
AR              := $(TARGET_TOOLCHAIN_PREFIX)ar 
LD              := $(TARGET_TOOLCHAIN_PREFIX)ld
NM              := $(TARGET_TOOLCHAIN_PREFIX)nm
OBJDUMP         := $(TARGET_TOOLCHAIN_PREFIX)objdump
OBJCOPY         := $(TARGET_TOOLCHAIN_PREFIX)objcopy
SIZE            := $(TARGET_TOOLCHAIN_PREFIX)size

INCLUDES := -I$(SOC_DIRECTORY)/software/include \
           -I$(BUILDINC_DIRECTORY) \
           -I$(CPU_DIRECTORY) \
		   -I$(ROOT_DIR)/include

LDSCRIPT := linker.ld
MAP_NAME := fw.map
COMMON_FLAGS := $(CPUFLAGS) -MD -MP -ffunction-sections -fdata-sections -fstack-usage -ffreestanding -Wall
ifeq ($(DEBUG),yes)
	COMMON_FLAGS += -g -Og
else
	COMMON_FLAGS += -DNDEBUG -Os
endif

CFLAGS = $(CFLAGS_ARGS) \
			$(COMMON_FLAGS) \
			-std=gnu11

CXXFLAGS = $(CXXFLAGS_ARGS) \
			$(COMMON_FLAGS) \
			-std=gnu++11 \
			-fno-exceptions \
			-fno-rtti \
			-fno-unwind-tables \
			-fcheck-new \
			-fno-use-cxa-atexit

LDFLAGS = -L$(BUILDINC_DIRECTORY) \
			-nostartfiles --specs=nano.specs \
			-Wl,--gc-sections,-Bstatic,-T,$(LDSCRIPT),-Map,$(MAP_NAME),--print-memory-usage

%.o: %.c
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

%.o: %.cc
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

%.o: %.S
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

%.hex: %.elf
	$(OBJCOPY) -O ihex $^ $@

%.bin: %.elf
	$(OBJCOPY) -O binary $^ $@

%.v: %.elf
	$(OBJCOPY) -O verilog $^ $@

%.asm: %.elf
	$(OBJDUMP) -S -d $^ > $@