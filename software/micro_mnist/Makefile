PROJ_NAME := micro_mnist

TARGET_DIR := $(realpath ../tote)
TARGET := tote

ROOTDIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
BUILDDIR := $(ROOTDIR)/build
OBJDIR := $(BUILDDIR)/obj
BINDIR := $(BUILDDIR)

TENSORFLOW_PATH ?= $(realpath ../../libs/tensorflow)

SRCS := $(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_mutable_op_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/memory_helpers.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/simple_memory_allocator.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/memory_planner/greedy_memory_planner.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_allocator.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_error_reporter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/debug_log_numbers.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/experimental/micro/micro_interpreter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/c/c_api_internal.c \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/error_reporter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/flatbuffer_conversions.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/op_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/tensor_utils.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/kernel_util.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/internal/quantization_util.cc  \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/debug_log.cc \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/kernels/depthwise_conv.cc \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/kernels/fully_connected.cc \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/kernels/pooling.cc \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/kernels/conv.cc \
		$(TARGET_DIR)/tensorflow/lite/experimental/micro/kernels/softmax.cc \
		src/model_data.S \
		src/main.cpp

OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.cpp,%.o,$(patsubst %.cc,%.o,$(patsubst %.c,%.o,$(SRCS))))))

include $(TARGET_DIR)/target.mk

INCLUDES += -I$(TENSORFLOW_PATH) -I.. -I../../libs/gemmlowp -I../../libs/flatbuffers/include

DEFS := -DNDEBUG -DTF_LITE_STATIC_MEMORY -DTF_LITE_MCU_DEBUG_LOG \
	-DTF_LITE_USE_GLOBAL_ROUND -DMODEL_DATA_FILE=\"src/model.tflite\" \
	-DTFLITE_EMULATE_FLOAT
CXXFLAGS += $(DEFS) -g -Os -funsigned-char -Wno-sign-compare
CCFLAGS += $(DEFS) -g -Os -funsigned-char -Wno-sign-compare

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

$(BINDIR)/$(PROJ_NAME).elf : $(OBJS) $(TARGET_OBJS)
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