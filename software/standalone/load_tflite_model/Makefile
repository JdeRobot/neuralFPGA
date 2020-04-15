PROJ_NAME := load_tflite_model

DEBUG := yes
STANDALONE := $(realpath ..)
TENSORFLOW_PATH := $(realpath ../../libs/tensorflow)
FLATBUFFERS_PATH := $(TENSORFLOW_PATH)/tensorflow/lite/tools/make/downloads/flatbuffers

INCLUDES += -I $(FLATBUFFERS_PATH)/include -I $(TENSORFLOW_PATH)

SRCS := $(wildcard src/*.c) \
		$(wildcard src/*.cpp) \
		$(wildcard src/*.S)

CFLAGS := -DNDEBUG -DMODEL_DATA_FILE=\"src/model.tflite\"
CXXFLAGS := -DNDEBUG

include ${STANDALONE}/common/bsp.mk
include ${STANDALONE}/common/riscv64-unknown-elf.mk
include ${STANDALONE}/common/standalone.mk
include ${STANDALONE}/common/build.mk
include ${STANDALONE}/common/test.mk
