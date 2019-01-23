TOP ?= not_defined_top
PIN_DEF = iCE40UP5K-B-EVN.pcf
DEVICE = up5k

ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJ_TOP_DIR = $(ROOT_DIR)/../..
TOP_LOWER_CASE := $(shell echo $(TOP) | tr '[:upper:]' '[:lower:]')

ARACHNE = arachne-pnr 
ARACHNE_ARGS = 
ICEPACK = icepack
ICETIME = icetime
ICEPROG = iceprog
ICEPROG_ARGS ?=

all: $(TOP).bin

%.v: $(PROJ_TOP_DIR)/src/main/scala/neuralfpga/$(TOP_LOWER_CASE)/$(TOP).scala
	cd $(PROJ_TOP_DIR) && sbt "runMain neuralfpga.$(TOP_LOWER_CASE).$(TOP) $(ROOT_DIR)"

%.blif: %.v
	yosys -p 'synth_ice40 -top $(TOP) -blif $@' $<

%.asc: $(PIN_DEF) %.blif
	$(ARACHNE) $(ARACHNE_ARGS) -d $(subst up,,$(subst hx,,$(subst lp,,$(DEVICE)))) -o $@ -p $^

%.bin: %.asc
	$(ICEPACK) $< $@

%.rpt: %.asc
	$(ICETIME) -d $(DEVICE) -mtr $@ $<

time: $(TOP).rpt

prog: $(TOP).bin
	$(ICEPROG) $(ICEPROG_ARGS) -S $<

clean:
	rm -f *.blif *.asc *.rpt *.bin *.v

.SECONDARY:
.PHONY: all prog time clean
