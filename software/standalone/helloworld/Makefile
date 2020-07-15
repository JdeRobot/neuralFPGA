PROJ_NAME := helloworld

DEBUG := yes
NEURALFPGA_ROOT_PATH := $(realpath ../../..)
BSP_PATH := ${NEURALFPGA_ROOT_PATH}/software/bsp
STANDALONE_PATH := ${NEURALFPGA_ROOT_PATH}/software/standalone

SRCS := $(wildcard src/*.c) \
		$(wildcard src/*.cpp) \
		$(wildcard src/*.S)


include ${BSP_PATH}/common/bsp.mk
include ${STANDALONE_PATH}/common/riscv64-unknown-elf.mk
include ${STANDALONE_PATH}/common/standalone.mk
include ${STANDALONE_PATH}/common/build.mk
include ${STANDALONE_PATH}/common/test.mk