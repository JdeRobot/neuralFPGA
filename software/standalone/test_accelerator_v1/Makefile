PROJ_NAME := accelerator_v1

DEBUG ?= yes
NEURALFPGA_ROOT_PATH := $(realpath ../../..)
BSP_PATH := ${NEURALFPGA_ROOT_PATH}/software/bsp
DRIVER_PATH := ${NEURALFPGA_ROOT_PATH}/software/driver
STANDALONE_PATH := ${NEURALFPGA_ROOT_PATH}/software/standalone

CFLAGS := -funsigned-char -Wno-sign-compare -std=gnu11

SRCS := $(wildcard src/*.c) \
		$(wildcard src/*.cpp) \
		$(wildcard src/*.S)


include ${BSP_PATH}/common/bsp.mk
include ${DRIVER_PATH}/driver.mk
include ${STANDALONE_PATH}/common/riscv64-unknown-elf.mk
include ${STANDALONE_PATH}/common/standalone.mk
include ${STANDALONE_PATH}/common/build.mk
include ${STANDALONE_PATH}/common/test.mk