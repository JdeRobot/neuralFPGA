package jderobot.lib.multiplier32bit

import jderobot.lib.blackbox.Booth_Multiplier_4xA
import spinal.core._
import spinal.lib._

case class Multiplier32bitImpl() {
  def oneCycleMultiplier(valid: Bool, multiplicand: SInt, multiplier: SInt, ready: Bool, result: SInt): Area = new Area {
    ready := RegNext(valid) init(False)
    result := ready ? RegNextWhen(multiplicand * multiplier, valid) | 0
  }

  def sequentialMultiplier(unrollFactor: Int = 1)(valid: Bool, multiplicand: SInt, multiplier: SInt, ready: Bool, result: SInt): Area = new Area {
    assert(isPow2(unrollFactor))
    val a = Reg(UInt(32 bits)) //multiplicand
    val accumulator = Reg(UInt(64 bits))
    val counter = Counter(32 / unrollFactor + 1)
    ready := counter.willOverflowIfInc
    result := ready ? accumulator.asSInt | 0

    when(valid) {
      when(valid.rise(False)) {
        // negate operands if multiplier is negative (msb == 1)
        a := (multiplier.msb ? -multiplicand | multiplicand).asUInt
        accumulator := (U(0, 32 bits) @@ (multiplier.msb ? -multiplier | multiplier).asUInt)
      } elsewhen(!ready) {
        counter.increment()
        val sumElements = ((0 until unrollFactor).map(i => accumulator(i) ? (a << i) | U(0)) :+ (accumulator >> 32))
        val sumResult =  sumElements.map(_.asSInt.resize(32 + unrollFactor).asUInt).reduceBalancedTree(_ + _)
        accumulator := (sumResult @@ accumulator(31 downto 0)) >> unrollFactor
      }
    } otherwise {
      counter.clear()
    }
  }

  def boothMultiplier(valid: Bool, multiplicand: SInt, multiplier: SInt, ready: Bool, result: SInt): Area = new Area {
    val boothMul = Booth_Multiplier_4xA(operandsWidth = 32)
    boothMul.M := multiplicand.asBits
    boothMul.R := multiplier.asBits
    boothMul.Ld := valid.rise(False)
    boothMul.Rst := False
    ready := boothMul.Valid
    result := boothMul.P.asSInt
  }
}


case class Multiplier32bit(multImpl: (Bool, SInt, SInt, Bool, SInt) => Area) extends Component {
  val io = new Bundle {
    val valid = in Bool
    val multiplicand = in SInt(32 bits)
    val multiplier = in SInt(32 bits)

    val ready = out Bool
    val result = out SInt(64 bits)
  }

  val impl = multImpl(io.valid, io.multiplicand, io.multiplier, io.ready, io.result)
}

object Multiplier32bit {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "."
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultClockDomainFrequency = FixedFrequency(12 MHz),
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(Multiplier32bit(Multiplier32bitImpl().boothMultiplier))
  }
}