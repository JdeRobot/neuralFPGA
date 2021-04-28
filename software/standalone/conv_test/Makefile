PROJ_NAME := conv_test

DEBUG := yes
NEURALFPGA_ROOT_PATH := $(realpath ../../..)
BSP_PATH := ${NEURALFPGA_ROOT_PATH}/software/bsp
DRIVER_PATH := ${NEURALFPGA_ROOT_PATH}/software/driver
STANDALONE_PATH := ${NEURALFPGA_ROOT_PATH}/software/standalone
TENSORFLOW_PATH := ${NEURALFPGA_ROOT_PATH}/software/libs/tensorflow
TENSORFLOW_EXTENSION_PATH := ${NEURALFPGA_ROOT_PATH}/software/tensorflow
FLATBUFFERS_PATH := $(TENSORFLOW_PATH)/tensorflow/lite/tools/make/downloads/flatbuffers
GEMMLOWP_PATH := $(TENSORFLOW_PATH)/tensorflow/lite/tools/make/downloads/gemmlowp
ABSL_PATH := $(TENSORFLOW_PATH)/tensorflow/lite/tools/make/downloads/absl

INCLUDES += -I$(TENSORFLOW_PATH) -I$(GEMMLOWP_PATH) -I$(FLATBUFFERS_PATH)/include -I$(ABSL_PATH)

DEFS := -DNDEBUG \
		-DTF_LITE_STATIC_MEMORY \
		-DTF_LITE_MCU_DEBUG_LOG \
		-DTF_LITE_USE_GLOBAL_CMATH_FUNCTIONS \
		-DTFLITE_EMULATE_FLOAT
CFLAGS := $(DEFS) -funsigned-char -Wno-sign-compare -std=gnu11
CXXFLAGS := $(DEFS) -funsigned-char -Wno-sign-compare -std=gnu++11

LIBS := -lm


SRCS := $(filter-out %_test.cc, $(wildcard $(TENSORFLOW_PATH)/tensorflow/lite/micro/*.cc)) \
		$(filter-out %_test.cc, $(wildcard $(TENSORFLOW_PATH)/tensorflow/lite/micro/memory_planner/*.cc)) \
		$(TENSORFLOW_PATH)/tensorflow/lite/c/common.c \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/error_reporter.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/flatbuffer_conversions.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/op_resolver.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/core/api/tensor_utils.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/internal/quantization_util.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/kernels/kernel_util.cc \
		$(TENSORFLOW_PATH)/tensorflow/lite/micro/testing/test_utils.cc \
		$(TENSORFLOW_EXTENSION_PATH)/lite/micro/kernels/conv.cc \
		src/conv_test.cc


include ${BSP_PATH}/common/bsp.mk
include ${DRIVER_PATH}/driver.mk
include ${STANDALONE_PATH}/common/riscv64-unknown-elf.mk
include ${STANDALONE_PATH}/common/standalone.mk
include ${STANDALONE_PATH}/common/build.mk
include ${STANDALONE_PATH}/common/test.mk