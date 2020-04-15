PROJ_NAME := helloworld

DEBUG := yes
STANDALONE := $(realpath ..)

SRCS := $(wildcard src/*.c) \
		$(wildcard src/*.cpp) \
		$(wildcard src/*.S)


include ${STANDALONE}/common/bsp.mk
include ${STANDALONE}/common/riscv64-unknown-elf.mk
include ${STANDALONE}/common/standalone.mk
include ${STANDALONE}/common/build.mk
include ${STANDALONE}/common/test.mk