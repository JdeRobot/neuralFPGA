CXX ?= clang++
PKG_CONFIG ?= pkg-config
PROJ_ROOT = ../../../..

GEMMLOWP_PATH ?= $(PROJ_ROOT)/libs/gemmlowp

CXXFLAGS ?= -g --std=c++11 -I $(GEMMLOWP_PATH)/public $(shell $(PKG_CONFIG) --cflags jsoncpp)
LDLIBS ?= -lpthread $(shell $(PKG_CONFIG) --libs jsoncpp)

ALL_TARGETS = rounding_divide_by_pot_8bit_reference \
	rounding_divide_by_pot_32bit_reference \
	saturating_rounding_doubling_high_mul_32bit_reference

all: $(ALL_TARGETS)
	

%.o: %cpp
	$(CXX) -c -o $@ $< $(CXXFLAGS)

rounding_divide_by_pot_32bit_reference: rounding_divide_by_pot_32bit_reference.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

rounding_divide_by_pot_8bit_reference: rounding_divide_by_pot_8bit_reference.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

saturating_rounding_doubling_high_mul_32bit_reference: saturating_rounding_doubling_high_mul_32bit_reference.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

saturating_rounding_doubling_high_mul_8bit_reference: saturating_rounding_doubling_high_mul_8bit_reference.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

check:
	@$(PKG_CONFIG) --print-errors --exists jsoncpp

clean:
	rm -f *.o $(ALL_TARGETS)

.PHONY: all clean check
