PROJ_NAME := micro_mnist
include ../common.mk

TFMICRO_PATH := $(ROOT_DIR)/tfmicro
FLATBUFFERS_PATH := $(TFMICRO_PATH)/third_party/flatbuffers
GEMMLOWP_PATH := $(TFMICRO_PATH)/third_party/gemmlowp
ABSL_PATH := $(TFMICRO_PATH)/third_party/absl
RUY_PATH := $(TFMICRO_PATH)/third_party/ruy

INCLUDES += -I$(TFMICRO_PATH) \
			-I$(GEMMLOWP_PATH) \
			-I$(FLATBUFFERS_PATH)/include \
			-I$(ABSL_PATH) \
			-I$(RUY_PATH)

DEFS := -DTF_LITE_STATIC_MEMORY \
		-DTF_LITE_MCU_DEBUG_LOG \
		-DTF_LITE_USE_GLOBAL_CMATH_FUNCTIONS \
		-DTF_LITE_USE_GLOBAL_MIN \
		-DTF_LITE_USE_GLOBAL_MAX \
		-DMODEL_DATA_FILE=\"model.tflite\" \
		-DTEST_IMAGES_DATA_FILE=\"test_images.bin\" \
		-DTEST_LABELS_DATA_FILE=\"test_labels.bin\" \
		-DTFLITE_EMULATE_FLOAT
CFLAGS += $(DEFS) -funsigned-char -Wno-sign-compare
CXXFLAGS += $(DEFS) -funsigned-char -Wno-sign-compare
LIBS += -lm -lsupc++

MICROLITE_CC_KERNEL_SRCS := \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/activations.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/add.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/arg_min_max.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/batch_to_space_nd.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/cast.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/ceil.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/circular_buffer.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/comparisons.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/concatenation.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/conv.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/conv_test_common.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/depthwise_conv.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/dequantize.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/detection_postprocess.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/elementwise.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/ethosu.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/exp.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/flexbuffers_generated_data.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/floor.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/fully_connected.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/fully_connected_common.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/hard_swish.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/kernel_runner.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/kernel_util.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/l2norm.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/logical.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/logistic.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/maximum_minimum.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/mul.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/neg.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/pack.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/pad.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/pooling.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/prelu.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/quantize.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/quantize_common.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/reduce.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/reshape.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/resize_nearest_neighbor.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/round.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/shape.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/softmax.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/softmax_common.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/space_to_batch_nd.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/split.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/split_v.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/strided_slice.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/sub.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/svdf.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/svdf_common.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/tanh.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/transpose_conv.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/unpack.cc \
$(TFMICRO_PATH)/tensorflow/lite/micro/kernels/zeros_like.cc 

MICROLITE_CC_SRCS := \
$(filter-out %debug_log.cc %micro_time.cc, $(wildcard $(TFMICRO_PATH)/tensorflow/lite/micro/*.cc)) \
$(wildcard $(TFMICRO_PATH)/tensorflow/lite/micro/memory_planner/*.cc) \
$(TFMICRO_PATH)/tensorflow/lite/c/common.c \
$(TFMICRO_PATH)/tensorflow/lite/core/api/error_reporter.cc \
$(TFMICRO_PATH)/tensorflow/lite/core/api/flatbuffer_conversions.cc \
$(TFMICRO_PATH)/tensorflow/lite/core/api/op_resolver.cc \
$(TFMICRO_PATH)/tensorflow/lite/core/api/tensor_utils.cc \
$(TFMICRO_PATH)/tensorflow/lite/kernels/internal/quantization_util.cc \
$(TFMICRO_PATH)/tensorflow/lite/kernels/kernel_util.cc \
$(TFMICRO_PATH)/tensorflow/lite/schema/schema_utils.cc

SRCS := $(MICROLITE_CC_KERNEL_SRCS) \
		$(MICROLITE_CC_SRCS) \
		model_data.S \
		test_data.S \
		main.cpp \
		isr.c \
		debug_log.cc \
		micro_time.cc

OBJS := $(SRCS)
OBJS := $(OBJS:.c=.o)
OBJS := $(OBJS:.cc=.o)
OBJS := $(OBJS:.cpp=.o)
OBJS := $(OBJS:.S=.o)
OBJS := $(OBJS:.s=.o)
OBJS := $(ROOT_DIR)/rt/crt0.o \
		$(ROOT_DIR)/rt/init.o \
		uart.o \
		$(OBJS)

LDSCRIPT := linker.ld

all: $(PROJ_NAME).elf $(PROJ_NAME).asm $(PROJ_NAME).bin

$(PROJ_NAME).elf: $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS) $(LIBS)

uart.o: $(LIBBASE_DIRECTORY)/uart.c
	$(CC) $(CFLAGS) -I$(SOC_DIRECTORY)/software/include/base $(INCLUDES) -c $< -o $@

-include $(OBJS:.o=.d)

.PHONY : clean
clean:
	echo $(ROOT_DIR)
	-rm -f $(OBJS) $(OBJS:.o=.d) $(OBJS:.o=.su) $(PROJ_NAME).elf $(PROJ_NAME).asm $(PROJ_NAME).bin $(MAP_NAME)

.SECONDARY: $(OBJS)