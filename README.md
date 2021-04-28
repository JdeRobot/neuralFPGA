Neural FPGA
============
This repository contains the Neural FPGA project.

## About the Neural FPGA project
Neural FPGA project goal is to produce custom hardware able to do inference over generic neural networks using only open source tools. In order to test our hardware designs we rely on hardware simulations and FPGAs.

Hardware simulation open source tools have been available since long time (see [Verilator](https://www.veripool.org/wiki/verilator), [Icarus Verilog](http://iverilog.icarus.com/)), but open source tools to generate bitstreams for FPGAs were lacking until recent. Project [IceStorm](http://www.clifford.at/icestorm/) was the first complete tool to program a commercially available FPGA, the [Lattice iCE40](http://www.latticesemi.com/Products/FPGAandCPLD/iCE40.aspx). Following, the project [prjtrellis](https://github.com/SymbiFlow/prjtrellis) is documenting the bitstream format for the more capable [Lattice ECP5](http://www.latticesemi.com/Products/FPGAandCPLD/ECP5). And others are comming.

On this project we won't focus on specific FPGAs, but because we would like to run our designs on real hardware we will try to fit our designs to available hardware with open source tools. This for the time being will be Ice40 and ECP5 from Lattice.

The main output of this project are cores that can do inference on generic neural networks trained with TensorFlow.

## Quick start
The project depends on several tools, check the requirements on each project to set up your dev environment:

1. [SpinalHDL](https://github.com/SpinalHDL/SpinalHDL) for the hardware description language.
2. [VexRiscv](https://github.com/SpinalHDL/VexRiscv) used as softcore. Check details on how to install [openOCD](https://github.com/SpinalHDL/VexRiscv#interactive-debug-of-the-simulated-cpu-via-gdb-openocd-and-verilator)
2. [Verilator](https://www.veripool.org/wiki/verilator) for the hardware simulations
3. RISC-V GNU Embedded Toolchain to build the software targetted to riscv. YOu can build it yourself (https://github.com/riscv/riscv-gnu-toolchain) or get a pre-built one from https://www.sifive.com or https://github.com/gnu-mcu-eclipse/riscv-none-gcc/releases
4. [Tensorflow](www.tensorflow.org): To train and do inference.

