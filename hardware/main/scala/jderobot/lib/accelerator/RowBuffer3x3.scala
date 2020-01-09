package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._

case class RowBuffer3x3Config(elementWidth: Int, maxRowWidth: Int) {
  def maxDelayValue(): Int  = maxRowWidth*2 + 3
}

case class RowBuffer3x3FlushCmd(config: RowBuffer3x3Config) extends Bundle{
  val rowWidth = UInt (log2Up(config.maxRowWidth + 1) bit)
  val initialDelay = UInt (log2Up((config.maxDelayValue()) + 1) bit)
}

case class RowBuffer3x3(config: RowBuffer3x3Config) extends Component {
  require(config.maxRowWidth >= 1)
  require(isPow2(config.maxRowWidth))

  val io = new Bundle {
    val flush = slave Flow(RowBuffer3x3FlushCmd(config))
    val input = slave Flow(Bits(config.elementWidth bits))
    val output = master Flow(Vec(Bits(config.elementWidth bits), 9))
  }

  val rowShifter = new Area {
    val row0 = new Mem(Bits(config.elementWidth bits), config.maxRowWidth) addAttribute(Verilator.public)
    val row1 = new Mem(Bits(config.elementWidth bits), config.maxRowWidth) addAttribute(Verilator.public)
    val readPtr = Counter(config.maxRowWidth)
    val writePtr = UInt(log2Up(config.maxRowWidth) bit)
    val writeOffset = Reg(io.flush.rowWidth) init(0)

    val row0Front = row0.readAsync(readPtr.value)
    val row1Front = row1.readAsync(readPtr.value)

    writePtr := (readPtr.value + writeOffset).resized

    when(io.input.valid) {
      row0(writePtr) := io.input.payload
      row1(writePtr) := row0Front
      readPtr.increment()
    }

    when(io.flush.valid) {
      readPtr.clear()
      writeOffset := io.flush.rowWidth
    }
  }

  val windowCache3x3 = new Area {
    val input0 = io.input.payload
    val input1 = rowShifter.row0Front
    val input2 = rowShifter.row1Front

    val shifting = io.input.valid

    val row0_0 = RegNextWhen(input0, shifting) init(0)
    val row0_1 = RegNextWhen(row0_0, shifting) init(0)
    val row0_2 = RegNextWhen(row0_1, shifting) init(0)

    val row1_0 = RegNextWhen(input1, shifting) init(0)
    val row1_1 = RegNextWhen(row1_0, shifting) init(0)
    val row1_2 = RegNextWhen(row1_1, shifting) init(0)

    val row2_0 = RegNextWhen(input2, shifting) init(0)
    val row2_1 = RegNextWhen(row2_0, shifting) init(0)
    val row2_2 = RegNextWhen(row2_1, shifting) init(0)

    val output = Vec(row0_0, row0_1, row0_2, row1_0, row1_1, row1_2, row2_0, row2_1, row2_2)
  }

  val delayValueNext = UInt(log2Up((config.maxDelayValue()) + 1) bit)
  val delay = RegNext(delayValueNext) init(0)
  val delayWillDecrement = False

  delayValueNext := (delay - U(delayWillDecrement)).resized

  when(io.input.valid) {
    delayWillDecrement := (delay =/= 0)
  }

  when(io.flush.valid) {
    delayValueNext := io.flush.initialDelay
  }

  io.output.valid := io.input.valid && (delay === 0)
  io.output.payload := Vec(windowCache3x3.output.reverse)
}

object RowBuffer3x3 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(RowBuffer3x3(RowBuffer3x3Config(elementWidth = 8, maxRowWidth = 256)))
  }
}