package neuralfpga.core

import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusSlaveFactory}

case class MachineTimer() extends Component {
  val io = new Bundle {
    val bus = slave(PipelinedMemoryBus(4, 32))
    val mTimeInterrupt = out Bool()
  }

  val mapper = new PipelinedMemoryBusSlaveFactory(io.bus)
  val counter = Reg(UInt(64 bits)) init (0)
  val cmp = Reg(UInt(64 bits))
  val interrupt = RegInit(False) setWhen (!(counter - cmp).msb) clearWhen (mapper.isWriting(0x8) || mapper.isWriting(0xC))
  counter := counter + 1
  io.mTimeInterrupt := interrupt
  mapper.readMultiWord(counter, 0x0)
  mapper.writeMultiWord(cmp, 0x8)
}