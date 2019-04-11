PROJ_NAME := helloworld
DEBUG := no

TARGET_DIR := $(realpath ../tote)
TARGET := tote
ROOTDIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
BUILDDIR := $(ROOTDIR)/build
OBJDIR := $(BUILDDIR)/obj
BINDIR := $(BUILDDIR)

SRCS := $(wildcard src/*.c) $(wildcard src/*.S)
OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.cpp,%.o,$(patsubst %.c,%.o,$(SRCS)))))
TARGET_SRCS := $(wildcard $(TARGET_DIR)/riscv/*.c) $(wildcard $(TARGET_DIR)/riscv/*.S)
TARGET_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.c,%.o,$(TARGET_SRCS))))

include $(TARGET_DIR)/target.mk

LDFLAGS += -ffreestanding -nostdlib 
#LDFLAGS += -specs=nano.specs -specs=nosys.specs #-lc_nano
LDFLAGS += -Wl,-Bstatic,-T,$(TARGET_DIR)/$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage

CCFLAGS += -I $(TARGET_DIR)/hal
CXXFLAGS += -I $(TARGET_DIR)/hal

all: $(BINDIR)/$(PROJ_NAME).bin $(BINDIR)/$(PROJ_NAME).asm

$(OBJDIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.S
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(BINDIR)/$(PROJ_NAME).elf : $(OBJS) $(TARGET_OBJS)
	$(CC) $(CCFLAGS) -o $@ $(OBJS) $(TARGET_OBJS) $(LDFLAGS)

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
