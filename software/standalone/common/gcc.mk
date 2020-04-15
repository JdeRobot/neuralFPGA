CC=gcc
CXX=g++
OBJCOPY=objcopy
OBJDUMP=objdump

ifeq ($(DEBUG),yes)
	CFLAGS += -g3 -Og
	CXXFLAGS += -g3 -Og
else
	CFLAGS += -O3
	CXXFLAGS += -O3
endif

