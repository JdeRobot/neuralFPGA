TOPLEVEL ?= not_defined_top
PIN_DEF = iCE40UP5K-B-EVN.pcf
DEVICE = up5k

ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJ_TOP_DIR = $(ROOT_DIR)/../..

ARACHNE = arachne-pnr 
ARACHNE_ARGS = 
ICEPACK = icepack
ICETIME = icetime
ICEPROG = iceprog
ICEPROG_ARGS ?=

all: $(TOPLEVEL).bin

%.v: ${PROJ_TOP_DIR}/hardware/main/scala/neuralfpga/umlp/UMlpUp5k.scala
	cd ${PROJ_TOP_DIR} && sbt "runMain neuralfpga.umlp.${TOPLEVEL} ${ROOT_DIR}"

%.blif: %.v
	yosys -p 'synth_ice40 -top $(TOPLEVEL) -blif $@' $<

%.asc: $(PIN_DEF) %.blif
	$(ARACHNE) $(ARACHNE_ARGS) -d $(subst up,,$(subst hx,,$(subst lp,,$(DEVICE)))) -o $@ -p $^

%.bin: %.asc
	$(ICEPACK) $< $@

%.rpt: %.asc
	$(ICETIME) -d $(DEVICE) -mtr $@ $<

time: $(TOPLEVEL).rpt

prog: $(TOPLEVEL).bin
	$(ICEPROG) $(ICEPROG_ARGS) -S $<

clean:
	rm -f *.blif *.asc *.rpt *.bin *.v

.SECONDARY:
.PHONY: all prog time clean
