package jderobot.lib.blackbox

import math.log
import spinal.core._

case class Booth_Multiplier(operandsWidth: Int = 8) extends BlackBox {
  addGeneric("pN", (log(operandsWidth)/log(2)).toInt)

  val Rst = in Bool() // Reset
  val Clk = in Bool() // Clock
  val Ld = in Bool() // Load Registers and Start Multiplier
  val M = in Bits(operandsWidth bits) // Multiplicand
  val R = in Bits(operandsWidth bits) // Multiplier
  val Valid = out Bool() // Product Valid
  val P = out Bits(2*operandsWidth bits) // Product <= M * R


  mapCurrentClockDomain(Clk)
  addRTLPath("hardware/main/resources/rtl/Booth_Multipliers/Src/Booth_Multiplier.v")
}

case class Booth_Multiplier_4xA(operandsWidth: Int = 8) extends BlackBox {
  addGeneric("N", operandsWidth)

  val Rst = in Bool() // Reset
  val Clk = in Bool() // Clock
  val Ld = in Bool() // Load Registers and Start Multiplier
  val M = in Bits(operandsWidth bits) // Multiplicand
  val R = in Bits(operandsWidth bits) // Multiplier
  val Valid = out Bool() // Product Valid
  val P = out Bits(2*operandsWidth bits) // Product <= M * R


  mapCurrentClockDomain(Clk)
  addRTLPath("hardware/main/resources/rtl/Booth_Multipliers/Src/Booth_Multiplier_4xA.v")
}
