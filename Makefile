SCALA_DEPS :=  $(shell find hardware/main/scala -type f -name "*.scala")
RTL_DIRS = $(realpath rtl) \
		$(realpath hardware/main/resources/rtl/lattice/ice40)

all: totecore totesim test_reference

totecore: rtl/Tote.v

totesim: totesim/obj_dir/VTote_sim

totesim/obj_dir/VTote_sim: rtl/Tote.v
	$(MAKE) -C totesim RTL_DIRS="$(RTL_DIRS)" totesim

rtl/Tote.v: $(SCALA_DEPS)
	mkdir -p rtl
	sbt "runMain neuralfpga.core.Tote"

test: test_reference
	sbt test
	BSP=totesim $(MAKE) -C software/standalone test

test_reference:
	$(MAKE) -C hardware/test/cpp/reference

clean:
	rm -rf rtl
	$(MAKE) -C totesim clean
	$(MAKE) -C hardware/test/cpp/reference clean

.PHONY: all test clean totecore totesim test_reference