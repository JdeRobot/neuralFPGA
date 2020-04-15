SOFTWARE_PATH := $(realpath ../../../../software)
GEMMLOWP_PATH ?= $(SOFTWARE_PATH)/libs/tensorflow/tensorflow/lite/tools/make/downloads/gemmlowp

CXXFLAGS ?= --std=c++11 -I$(GEMMLOWP_PATH)/public

ALL_TARGETS = rounding_divide_by_pot_8bit_reference \
	rounding_divide_by_pot_32bit_reference \
	saturating_rounding_doubling_high_mul_32bit_reference

all: $(ALL_TARGETS)
	
%: %.cpp
	$(CXX) -o $@ $(CXXFLAGS) $^ $(LDLIBS)

clean:
	rm -f *.o $(ALL_TARGETS)

.PHONY: clean
