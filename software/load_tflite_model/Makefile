PROJ_NAME := load_tflite_model
DEBUG := no

TARGET_DIR := $(realpath ../tote)
TARGET := tote
ROOTDIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
BUILDDIR := $(ROOTDIR)/build
OBJDIR := $(BUILDDIR)/obj
BINDIR := $(BUILDDIR)

SRCS := src/main.cpp src/model_data.S
OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.cpp,%.o,$(SRCS))))
TARGET_SRCS := $(wildcard $(TARGET_DIR)/riscv/*.c) $(wildcard $(TARGET_DIR)/riscv/*.S)
TARGET_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.c,%.o,$(TARGET_SRCS))))
LIBC_SRCS := $(wildcard $(TARGET_DIR)/libc/stdio/*.c) $(wildcard $(TARGET_DIR)/libc/stdio/*.S) $(wildcard $(TARGET_DIR)/libc/stdlib/*.c)
LIBC_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.c,%.o,$(LIBC_SRCS))))
SUPCPP_SRCS := $(wildcard $(TARGET_DIR)/supc++/*.cpp)
SUPCPP_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.cpp,%.o,$(SUPCPP_SRCS)))

include $(TARGET_DIR)/target.mk

LDFLAGS += -ffreestanding -nostdlib
LDFLAGS += -lgcc
LDFLAGS += -Wl,-Bstatic,-T,$(TARGET_DIR)/$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage

CCFLAGS += -I $(TARGET_DIR)/hal -DNDEBUG -DMODEL_DATA_FILE=\"src/model.tflite\" -g -Os
CXXFLAGS += -I $(TARGET_DIR)/hal -fcheck-new -DNDEBUG -I /home/dlobato/src/flatbuffers/include -g -Os #-fno-exceptions -fno-rtti -fno-use-cxa-atexit

all: $(BINDIR)/$(PROJ_NAME).bin $(BINDIR)/$(PROJ_NAME).asm

$(OBJDIR)/%.o: %.tflite
	@mkdir -p $(dir $@)
	$(OBJCOPY) -I binary -O elf32-littleriscv -B riscv:rv32 $< $@

$(OBJDIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.S
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(BINDIR)/$(PROJ_NAME).elf : $(OBJS) $(TARGET_OBJS) $(LIBC_OBJS) $(SUPCPP_OBJS)
	$(CXX) $(CCFLAGS) -o $@ $^ $(LDFLAGS)

%.bin: %.elf
	@mkdir -p $(dir $@)
	$(OBJCOPY) $< $@ -O binary

%.hex: %.elf
	@mkdir -p $(dir $@)
	$(OBJCOPY) $< $@ -O ihex

%.v: %.elf
	$(OBJCOPY) $< $@ -O verilog

%.asm: %.elf
	$(OBJDUMP) -S -d $< > $@

.PHONY: clean
clean:
	rm -rf $(OBJDIR)
	rm -f $(BINDIR)/*
