PROJ_NAME=blink
DEBUG=no
MULDIV=no
COMPRESSED=no

CFLAGS = -I ../include

SRCS = 	$(wildcard src/*.c)    \
		$(wildcard src/*.cpp)  \
		$(wildcard src/*.S)


LDSCRIPT = ../standalone/ld/link.ld

include ../standalone/mk/riscv64-unknown-elf.mk
include ../standalone/mk/standalone.mk

