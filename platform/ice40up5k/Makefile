TOPLEVEL ?= not_defined_top
PIN_DEF = iCE40UP5K-B-EVN.pcf
DEVICE = up5k

ROOT_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJ_TOP_DIR = $(ROOT_DIR)/../..

ARACHNE = arachne-pnr 
ARACHNE_ARGS =
NEXTPNR = nextpnr-ice40
NEXTPNR_ARGS = --freq 12
ICEPACK = icepack
ICETIME = icetime
ICEPROG = iceprog
ICEPROG_ARGS ?=

all: $(TOPLEVEL).bin

%.v:
	cd ${PROJ_TOP_DIR} && sbt "runMain neuralfpga.core.${TOPLEVEL} ${ROOT_DIR}"

%.blif: %.v
	yosys -p 'synth_ice40 -top $(TOPLEVEL) -blif $@' $<

%.json: %.v
	yosys -p 'synth_ice40 -top $(TOPLEVEL) -json $@' $<

%.asc: %.json $(PIN_DEF)
	$(NEXTPNR) $(NEXTPNR_ARGS) --$(DEVICE) --json $< --pcf $(PIN_DEF) --asc $@

#%.asc: $(PIN_DEF) %.blif
	#$(ARACHNE) $(ARACHNE_ARGS) -d $(subst up,,$(subst hx,,$(subst lp,,$(DEVICE)))) -o $@ -p $^

%.bin: %.asc
	$(ICEPACK) $< $@

%.rpt: %.asc
	$(ICETIME) -d $(DEVICE) -mtr $@ $<

time: $(TOPLEVEL).rpt

prog: $(TOPLEVEL).bin
	$(ICEPROG) $(ICEPROG_ARGS) -S $<

clean:
	rm -f *.json *.blif *.asc *.rpt *.bin *.v

.SECONDARY:
.PHONY: all prog time clean
