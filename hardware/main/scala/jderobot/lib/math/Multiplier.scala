package jderobot.lib.math

import jderobot.lib.blackbox.Booth_Multiplier_4xA
import spinal.core._

case class MultiplierImpl() {
  def oneCycleMultiplier(valid: Bool, multiplicand: SInt, multiplier: SInt, ready: Bool, result: SInt): Area = new Area {
    ready := RegNext(valid) init(False)
    result := ready ? RegNextWhen(multiplicand * multiplier, valid) | 0
  }

  def boothMultiplier(valid: Bool, multiplicand: SInt, multiplier: SInt, ready: Bool, result: SInt): Area = new Area {
    val boothMul = Booth_Multiplier_4xA(operandsWidth = multiplicand.getWidth)
    boothMul.M := multiplicand.asBits
    boothMul.R := multiplier.asBits
    boothMul.Ld := valid.rise(False)
    boothMul.Rst := False
    ready := boothMul.Valid
    result := boothMul.P.asSInt
  }
}


case class Multiplier(operandsWidth: Int = 8,
                       multImpl: (Bool, SInt, SInt, Bool, SInt) => Area
                     ) extends Component {
  val io = new Bundle {
    val valid = in Bool
    val multiplicand = in SInt(operandsWidth bits)
    val multiplier = in SInt(operandsWidth bits)

    val ready = out Bool
    val result = out SInt(operandsWidth*2 bits)
  }

  val impl = multImpl(io.valid, io.multiplicand, io.multiplier, io.ready, io.result)
}

object Multiplier {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "."
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultClockDomainFrequency = FixedFrequency(12 MHz),
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(Multiplier(operandsWidth = 32, multImpl = MultiplierImpl().boothMultiplier))
  }
}