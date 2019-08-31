interface remote_bitbang
remote_bitbang_port 9090
remote_bitbang_host localhost

set  _ENDIAN little
set _TAP_TYPE 1234

if { [info exists CPUTAPID] } {
   set _CPUTAPID $CPUTAPID
} else {
  # set useful default
   set _CPUTAPID 0x10001fff 
}

#adapter_khz 4000
#adapter_nsrst_delay 260
#jtag_ntrst_delay 250

set _CHIPNAME fpga_spinal
jtag newtap $_CHIPNAME bridge -expected-id $_CPUTAPID -irlen 4 -ircapture 0x1 -irmask 0xF 

target create $_CHIPNAME.cpu0 vexriscv -endian $_ENDIAN -chain-position $_CHIPNAME.bridge -coreid 0 -dbgbase 0xF00F0000
vexriscv readWaitCycles 12
vexriscv cpuConfigFile $VEXRISCV_YAML


poll_period 50



init
#echo "Halting processor"
soft_reset_halt
