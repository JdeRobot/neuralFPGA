PROJ_NAME := fully_connected_test

TARGET_DIR := $(realpath ../tote)
TARGET := tote

ROOTDIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
BUILDDIR := $(ROOTDIR)/build
OBJDIR := $(BUILDDIR)/obj
BINDIR := $(BUILDDIR)

TENSORFLOW_PATH ?= $(HOME)/src/tensorflow

SRCS := $(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_error_reporter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_mutable_op_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/debug_log.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/debug_log_numbers.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/simple_tensor_allocator.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_interpreter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/depthwise_conv.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/softmax.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/pooling.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/all_ops_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/fully_connected.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/c/c_api_internal.c \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/error_reporter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/flatbuffer_conversions.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/op_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/kernel_util.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/internal/quantization_util.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/kernels/fully_connected_test.cc
		#src/main.cpp

TARGET_SRCS := $(TARGET_DIR)/riscv/start.S $(TARGET_DIR)/riscv/init.c
LIBC_SRCS := $(wildcard $(TARGET_DIR)/libc/stdio/*.c) $(wildcard $(TARGET_DIR)/libc/stdio/*.S) $(wildcard $(TARGET_DIR)/libc/stdlib/*.c)
SUPCPP_SRCS := $(wildcard $(TARGET_DIR)/supc++/*.cpp)

OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.cpp,%.o,$(patsubst %.cc,%.o,$(patsubst %.c,%.o,$(SRCS)))))
TARGET_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.cpp,%.o,$(patsubst %.S,%.o,$(patsubst %.c,%.o,$(TARGET_SRCS)))))
LIBC_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.c,%.o,$(LIBC_SRCS))))
SUPCPP_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.cpp,%.o,$(SUPCPP_SRCS)))

include $(TARGET_DIR)/target.mk

LDFLAGS += -ffreestanding -nostdlib
LDFLAGS += -lm -lc -lgcc
LDFLAGS += -Wl,--gc-sections,-Bstatic,-T,$(TARGET_DIR)/$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage

INCLUDES := -I$(TENSORFLOW_PATH) -I../../libs/gemmlowp -I../../libs/flatbuffers/include -I $(TARGET_DIR)/hal

CXXFLAGS += -DNDEBUG -DTF_LITE_STATIC_MEMORY -DTF_LITE_MCU_DEBUG_LOG -DTF_LITE_USE_GLOBAL_ROUND 
CCFLAGS +=  -DNDEBUG -DTF_LITE_STATIC_MEMORY -DTF_LITE_MCU_DEBUG_LOG -DTF_LITE_USE_GLOBAL_ROUND

CXXFLAGS += -g -Os -fno-exceptions -fno-rtti -fno-unwind-tables -ffunction-sections -fdata-sections -funsigned-char -fcheck-new -Wall -Wno-sign-compare
CCFLAGS += -g -Os -fno-exceptions -fno-unwind-tables -ffunction-sections -fdata-sections -funsigned-char -Wall -Wno-sign-compare

all: $(BINDIR)/$(PROJ_NAME).bin $(BINDIR)/$(PROJ_NAME).asm

$(OBJDIR)/%.o: %.cc
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.c
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(OBJDIR)/%.o: %.S
	@mkdir -p $(dir $@)
	$(CC) $(CCFLAGS) $(INCLUDES) -c $< -o $@

$(BINDIR)/$(PROJ_NAME): $(OBJS) $(TARGET_OBJS) $(LIBC_OBJS) $(SUPCPP_OBJS)
	$(CXX) $(CCFLAGS) -o $@ $^ $(LDFLAGS)

%.bin: $(BINDIR)/$(PROJ_NAME)
	@mkdir -p $(dir $@)
	$(OBJCOPY) $< $@ -O binary

%.hex: $(BINDIR)/$(PROJ_NAME)
	@mkdir -p $(dir $@)
	$(OBJCOPY) $< $@ -O ihex

%.v: $(BINDIR)/$(PROJ_NAME)
	$(OBJCOPY) $< $@ -O verilog

%.asm: $(BINDIR)/$(PROJ_NAME)
	$(OBJDUMP) -S -d $< > $@

.PHONY: clean
clean:
	rm -rf $(OBJDIR)
	rm -f $(BINDIR)/*