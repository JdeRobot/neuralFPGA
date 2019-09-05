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

### Generate core HDL
To generate verilog for a core run:
```
$> cd neuralFPGA
$> sbt "runMain neuralfpga.core.Tote rtl"
```
This will generate the file _rtl/Tote.v_ and _cpu0.yaml_ (used on openocd session)

### Run simulation
First we need to build the simulator for our core:
```
$> cd neuralFPGA/sim/tote
$> make
```
Then we need a program to run on our simulated core. You can use the [hello world example](software/helloworld). Build it with:
```
$> cd neuralFPGA/software/helloworld
$> make
```
Run it on the simulator:
```
$> cd neuralFPGA/sim/tote
$> obj_dir/VTote_tb --program ../../software/helloworld/build/helloworld.elf
Loading program: ../../software/helloworld/build/helloworld.elf
LOAD  [0] 0x5	0x80000000	0x22f4	0x22f4
LOAD  [1] 0x6	0x80002300	0x70	0x80
LOAD  [2] 0x6	0x80002370	0x0	0x10
LOAD  [3] 0x6	0x80002370	0x0	0x800
LOAD  [4] 0x6	0x80002370	0x0	0x4000
_exit at 800000b4
Simulation start
586: Hello World!
_exit with code=0
```

### Debug simulation
The simulated core can be debugged with gdb attached to openocd.
Start the simulator:
```
$> cd neuralFPGA/sim/tote
$> obj_dir/VTote_tb --jtag-enabled
Simulation start
This emulator compiled with JTAG Remote Bitbang client.
Listening on port 9090
Attempting to accept client socket
```

On a different terminal start openocd:
```
$> cd neuralFPGA
$> openocd -c "set VEXRISCV_YAML cpu0.yaml" -f sim/openocd/tote_remote_bitbang.tcl
Open On-Chip Debugger 0.10.0+dev-01214-g0ace94f7 (2019-08-27-18:21)
Licensed under GNU GPL v2
For bug reports, read
	http://openocd.org/doc/doxygen/bugs.html
cpu0.yaml
Warn : Adapter driver 'remote_bitbang' did not declare which transports it allows; assuming legacy JTAG-only
Info : only one transport option; autoselect 'jtag'
Info : set servers polling period to 50ms
Info : Initializing remote_bitbang driver
Info : Connecting to localhost:9090
Info : remote_bitbang driver initialized
Info : This adapter doesn't support configurable speed
Info : JTAG tap: fpga_spinal.bridge tap/device found: 0x10001fff (mfg: 0x7ff (<invalid>), part: 0x0001, ver: 0x1)
Info : Listening on port 3333 for gdb connections
requesting target halt and executing a soft reset
Info : Listening on port 6666 for tcl connections
Info : Listening on port 4444 for telnet connections
```

And last on another terminal start gdb:
```
$> cd neuralFPGA
$> riscv64-unknown-elf-gdb software/helloworld/build/helloworld.elf
GNU gdb (SiFive GDB 8.3.0-2019.05.3) 8.3
Copyright (C) 2019 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
Type "show copying" and "show warranty" for details.
This GDB was configured as "--host=x86_64-linux-gnu --target=riscv64-unknown-elf".
Type "show configuration" for configuration details.
For bug reporting instructions, please see:
<https://github.com/sifive/freedom-tools/issues>.
Find the GDB manual and other documentation resources online at:
    <http://www.gnu.org/software/gdb/documentation/>.

For help, type "help".
Type "apropos word" to search for commands related to "word"...
Reading symbols from software/helloworld/build/helloworld.elf...
(gdb)
```

Now we need to connect our gdb to the target:
```
(gdb) target remote localhost:3333
Remote debugging using localhost:3333
_start ()
    at /home/dlobato/workspace-roboclub/neuralFPGA/software/tote/riscv/start.S:7
7	  la gp, __global_pointer$
(gdb) monitor reset halt
JTAG tap: fpga_spinal.bridge tap/device found: 0x10001fff (mfg: 0x7ff (<invalid>), part: 0x0001, ver: 0x1)
(gdb) load
Loading section .text, size 0x22f0 lma 0x80000000
Loading section .sdata2._global_impure_ptr, size 0x4 lma 0x800022f0
Loading section .sdata, size 0x10 lma 0x80002300
Loading section .data, size 0x60 lma 0x80002310
Start address 0x80000000, load size 9060
Transfer rate: 1474 KB/sec, 2265 bytes/write.
(gdb) continue
Continuing.
```

On the simulator terminal (the one running obj_dir/VTote_tb --jtag-enabled) we should see the message "9611126: Hello World!" (the timestamp might be different). You can stop the program execution on the gdb terminal with Ctrl + C.

Instead of continue we can set a breakpoint and debbug our code, print variable data, see registers, ...