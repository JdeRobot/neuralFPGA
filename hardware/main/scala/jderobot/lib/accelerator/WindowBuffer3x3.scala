package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._

case class WindowBuffer3x3Generics(elementWidth: Int, maxRowWidth: Int) {
  def maxDelayValue(): Int  = maxRowWidth*2 + 3
}

case class WindowBuffer3x3FlushCmd(config: WindowBuffer3x3Generics) extends Bundle{
  val rowWidth = UInt (log2Up(config.maxRowWidth + 1) bit)
  val initialDelay = UInt (log2Up((config.maxDelayValue()) + 1) bit)
}

case class WindowBuffer3x3(config: WindowBuffer3x3Generics) extends Component {
  require(config.maxRowWidth >= 1)
  require(isPow2(config.maxRowWidth))

  val io = new Bundle {
    val flush = slave Flow(WindowBuffer3x3FlushCmd(config))
    val input = slave Stream(Bits(config.elementWidth bits))
    val output = master Stream(Vec(Bits(config.elementWidth bits), 9))
  }

  val delayValueNext = UInt(log2Up((config.maxDelayValue()) + 1) bit)
  val delay = RegNext(delayValueNext) init(0)
  val delayWillDecrement = False
  val pendingRsp = RegInit(False)

  val rowShifter = new Area {
    val row0 = new Mem(Bits(config.elementWidth bits), config.maxRowWidth) addAttribute(Verilator.public)
    val row1 = new Mem(Bits(config.elementWidth bits), config.maxRowWidth) addAttribute(Verilator.public)
    val readPtr = Counter(config.maxRowWidth)
    val writePtr = UInt(log2Up(config.maxRowWidth) bit)
    val writeOffset = Reg(io.flush.rowWidth) init(0)

    val row0Front = row0.readAsync(readPtr.value)
    val row1Front = row1.readAsync(readPtr.value)

    writePtr := (readPtr.value + writeOffset).resized

    when(io.input.fire) {
      row0(writePtr) := io.input.payload
      row1(writePtr) := row0Front
      readPtr.increment()
    }

    when(io.flush.fire) {
      readPtr.clear()
      writeOffset := io.flush.rowWidth
    }
  }

  val windowCache3x3 = new Area {
    val input0 = io.input.payload
    val input1 = rowShifter.row0Front
    val input2 = rowShifter.row1Front

    val shift = io.input.fire

    val row0_0 = RegNextWhen(input0, shift) init(0)
    val row0_1 = RegNextWhen(row0_0, shift) init(0)
    val row0_2 = RegNextWhen(row0_1, shift) init(0)

    val row1_0 = RegNextWhen(input1, shift) init(0)
    val row1_1 = RegNextWhen(row1_0, shift) init(0)
    val row1_2 = RegNextWhen(row1_1, shift) init(0)

    val row2_0 = RegNextWhen(input2, shift) init(0)
    val row2_1 = RegNextWhen(row2_0, shift) init(0)
    val row2_2 = RegNextWhen(row2_1, shift) init(0)

    val output = Vec(row0_0, row0_1, row0_2, row1_0, row1_1, row1_2, row2_0, row2_1, row2_2)
  }

  delayValueNext := (delay - U(delayWillDecrement)).resized

  when(io.input.fire) {
    delayWillDecrement := (delay =/= 0)
  }

  when(io.flush.fire) {
    delayValueNext := io.flush.initialDelay
  }

  when(io.input.fire && (delay === 0)) {
    pendingRsp := True
  } elsewhen(io.output.fire) {
    pendingRsp := False
  }

  io.input.ready := ((delay =/= 0) || io.output.ready)
  io.output.valid :=  pendingRsp
  io.output.payload := Vec(windowCache3x3.output.reverse)
}

object WindowBuffer3x3 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(WindowBuffer3x3(WindowBuffer3x3Generics(elementWidth = 8, maxRowWidth = 256)))
  }
}