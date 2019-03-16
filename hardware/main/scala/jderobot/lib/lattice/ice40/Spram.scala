package jderobot.lib.lattice.ice40

import jderobot.lib.blackbox.lattice.ice40.SB_SPRAM256KA
import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusCmd}

// taken from https://github.com/SpinalHDL/SaxonSoc/blob/master/hardware/scala/saxon/Ice40Ip.scala
//Provide a 64 KB on-chip-ram via the Up5k SPRAM.
case class Spram() extends Component{
  val io = new Bundle{
    val bus = slave(PipelinedMemoryBus(16, 32))
  }

  val cmd = Flow(PipelinedMemoryBusCmd(io.bus.config))
  cmd << io.bus.cmd.toFlow

  val rspPending = RegNext(cmd.valid && !cmd.write) init(False)
  val rspTarget = RegNext(io.bus.cmd.valid)


  val mems = List.fill(2)(SB_SPRAM256KA())
  mems(0).DATAIN := cmd.data(15 downto 0)
  mems(0).MASKWREN := cmd.mask(1) ## cmd.mask(1) ## cmd.mask(0) ## cmd.mask(0)
  mems(1).DATAIN := cmd.data(31 downto 16)
  mems(1).MASKWREN := cmd.mask(3) ## cmd.mask(3) ## cmd.mask(2) ## cmd.mask(2)
  for(mem <- mems){
    mem.CHIPSELECT := cmd.valid
    mem.ADDRESS := (cmd.address >> 2).resized
    mem.WREN := cmd.write
    mem.STANDBY  := False
    mem.SLEEP    := False
    mem.POWEROFF := True
  }

  val readData = mems(1).DATAOUT ## mems(0).DATAOUT


  io.bus.rsp.valid := rspPending && rspTarget
  io.bus.rsp.data  := readData
}
