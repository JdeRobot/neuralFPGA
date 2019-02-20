package jderobot.lib.lattice.ice40

import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple.PipelinedMemoryBus

// taken from https://github.com/SpinalHDL/SaxonSoc/blob/master/hardware/scala/saxon/Ice40Ip.scala
case class Bram(onChipRamSize : BigInt) extends Component{
  val io = new Bundle{
    val bus = slave(PipelinedMemoryBus(32, 32))
  }

  val mem = Mem(Bits(32 bits), onChipRamSize / 4)
  io.bus.rsp.valid := RegNext(io.bus.cmd.fire && !io.bus.cmd.write) init(False)
  io.bus.rsp.data := mem.readWriteSync(
    address = (io.bus.cmd.address >> 2).resized,
    data  = io.bus.cmd.data,
    enable  = io.bus.cmd.valid,
    write  = io.bus.cmd.write,
    mask  = io.bus.cmd.mask
  )
  io.bus.cmd.ready := True
}
