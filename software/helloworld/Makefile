PROJ_NAME := helloworld

TARGET_DIR := $(realpath ../tote)
TARGET := tote
ROOTDIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
BUILDDIR := $(ROOTDIR)/build
OBJDIR := $(BUILDDIR)/obj
BINDIR := $(BUILDDIR)

include $(TARGET_DIR)/target.mk

CCFLAGS += -g -Os
CXXFLAGS += -g -Os

SRCS := src/main.c
OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.cpp,%.o,$(patsubst %.c,%.o,$(SRCS)))))

all: $(BINDIR)/$(PROJ_NAME).bin $(BINDIR)/$(PROJ_NAME).asm

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.S
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(BINDIR)/$(PROJ_NAME).elf : $(OBJS) $(TARGET_OBJS)
	$(CC) $(CCFLAGS) -o $@ $^ $(LDFLAGS)

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
