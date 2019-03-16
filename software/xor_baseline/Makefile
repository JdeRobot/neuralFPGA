PROJ_NAME=xor_baseline
DEBUG=no
MULDIV=no
COMPRESSED=no
TARGET=$(realpath ../standalone)

CFLAGS = -I $(TARGET)/include

SRCS = 	$(wildcard src/*.c) $(TARGET)/src/start.S $(TARGET)/src/init.c

LDSCRIPT = $(TARGET)/ld/link.ld

include $(TARGET)/mk/riscv64-unknown-elf.mk
include $(TARGET)/mk/target.mk

