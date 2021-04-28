#!/usr/bin/env python3

#
# Adapted from litex/litex/litex/tools/litex_sim.py
#
# Copyright (c) 2015-2020 Florent Kermarrec <florent@enjoy-digital.fr>
# Copyright (c) 2020 Antmicro <www.antmicro.com>
# Copyright (c) 2017 Pierre-Olivier Vauboin <po@lambdaconcept>
# SPDX-License-Identifier: BSD-2-Clause

import sys
import argparse

from migen import *

from litex.build.generic_platform import *
from litex.build.sim import SimPlatform
from litex.build.sim.config import SimConfig

from litex.soc.integration.common import *
from litex.soc.integration.soc_core import *
from litex.soc.integration.builder import *
from litex.soc.integration.soc import *
from litex.soc.cores.bitbang import *
from litex.soc.cores.cpu import CPUS


# IOs ----------------------------------------------------------------------------------------------

_io = [
    ("sys_clk", 0, Pins(1)),
    ("sys_rst", 0, Pins(1)),
    ("serial", 0,
        Subsignal("source_valid", Pins(1)),
        Subsignal("source_ready", Pins(1)),
        Subsignal("source_data",  Pins(8)),

        Subsignal("sink_valid",   Pins(1)),
        Subsignal("sink_ready",   Pins(1)),
        Subsignal("sink_data",    Pins(8)),
    )
]

# Platform -----------------------------------------------------------------------------------------

class Platform(SimPlatform):
    def __init__(self):
        SimPlatform.__init__(self, "SIM", _io)

# Simulation SoC -----------------------------------------------------------------------------------

class SimSoC(SoCCore):
    def __init__(self, **kwargs):
        platform     = Platform()
        sys_clk_freq = int(1e6)

        # SoCCore ----------------------------------------------------------------------------------
        SoCCore.__init__(self, platform, clk_freq=sys_clk_freq,
            ident         = "LiteX Simulation",
            ident_version = True,
            **kwargs)

        # CRG --------------------------------------------------------------------------------------
        self.submodules.crg = CRG(platform.request("sys_clk"))

        platform.add_debug(self, reset=0)

# Build --------------------------------------------------------------------------------------------

def generate_gtkw_savefile(builder, vns, trace_fst):
    from litex.build.sim import gtkwave as gtkw
    dumpfile = os.path.join(builder.gateware_dir, "sim.{}".format("fst" if trace_fst else "vcd"))
    savefile = os.path.join(builder.gateware_dir, "sim.gtkw")
    soc = builder.soc

    with gtkw.GTKWSave(vns, savefile=savefile, dumpfile=dumpfile) as save:
        save.clocks()
        save.fsm_states(soc)
        save.add(soc.bus.slaves["main_ram"], mappers=[gtkw.wishbone_sorter(), gtkw.wishbone_colorer()])


def sim_args(parser):
    builder_args(parser)
    soc_core_args(parser)
    parser.add_argument("--threads",              default=1,               help="Set number of threads (default=1)")
    parser.add_argument("--ram-init",             default=None,            help="ram_init file")
    parser.add_argument("--opt-level",            default="O3",            help="Compilation optimization level")

def main():
    parser = argparse.ArgumentParser(description="Generic LiteX SoC Simulation")
    sim_args(parser)
    args = parser.parse_args()

    soc_kwargs     = soc_core_argdict(args)
    builder_kwargs = builder_argdict(args)

    sys_clk_freq = int(1e6)
    sim_config = SimConfig()
    sim_config.add_clocker("sys_clk", freq_hz=sys_clk_freq)

    # Configuration --------------------------------------------------------------------------------

    cpu = CPUS[soc_kwargs.get("cpu_type", "vexriscv")]
    if soc_kwargs["uart_name"] == "serial":
        soc_kwargs["uart_name"] = "sim"
        sim_config.add_module("serial2console", "serial")
    soc_kwargs["integrated_main_ram_size"] = 0x10000000 # 256 MB
    if args.ram_init is not None:
        soc_kwargs["integrated_main_ram_init"] = get_mem_data(args.ram_init, cpu.endianness)

    # SoC ------------------------------------------------------------------------------------------
    soc = SimSoC(**soc_kwargs)
    soc.add_constant("ROM_BOOT_ADDRESS", 0x40000000)

    # Build/Run ------------------------------------------------------------------------------------
    builder_kwargs["csr_csv"] = "csr.csv"
    builder = Builder(soc, **builder_kwargs)
    builder.build(
        run         = False,
        threads     = args.threads,
        sim_config  = sim_config,
        opt_level   = args.opt_level
    )
        

if __name__ == "__main__":
    main()