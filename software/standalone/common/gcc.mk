CC=gcc
CXX=g++
OBJCOPY=objcopy
OBJDUMP=objdump

ifeq ($(DEBUG),yes)
	CFLAGS += -g
	CXXFLAGS += -g
else
	CFLAGS += -O3
	CXXFLAGS += -O3
endif

