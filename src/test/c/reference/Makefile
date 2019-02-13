CXX ?= clang++
PKG_CONFIG ?= pkg-config

GEMMLOWP_PATH ?= ../libs/gemmlowp

CXXFLAGS ?= -g --std=c++11 -I $(GEMMLOWP_PATH)/public $(shell $(PKG_CONFIG) --cflags jsoncpp)
LDLIBS ?= -lpthread $(shell $(PKG_CONFIG) --libs jsoncpp)

all: rounding_divide_by_pot_32bit_reference

%.o: %cpp
	$(CXX) -c -o $@ $< $(CXXFLAGS)

rounding_divide_by_pot_32bit_reference: rounding_divide_by_pot_32bit_reference.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

check:
	@$(PKG_CONFIG) --print-errors --exists jsoncpp

clean:
	rm -f *.o rounding_divide_by_pot_32bit_reference

.PHONY: all clean check
