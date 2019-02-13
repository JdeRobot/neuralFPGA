package jderobot.lib.math

import spinal.core._

/**
  * Correctly-rounded-to-nearest division by a power-of-two.
  */
object RoundingDivideByPOT {
  def apply(operand: SInt, x: UInt): SInt = {
    val signbit = U(operand.msb)
    val adjust = ((signbit << x) - signbit).resize(operand.getWidth).asSInt
    (operand + adjust) >> x
  }
}

/**
  * From gemmlowp fixedpoint.h
  *
  * Returns the integer that represents the product of two fixed-point
  * numbers, interpreting all integers as fixed-point values in the
  * interval [-1, 1), rounding to the nearest value, and saturating
  * -1 * -1 to the maximum value (since 1 is not in the half-open
  * interval [-1, 1)).
*/
object SaturatingRoundingDoublingHighMul {
  def apply(a: SInt, b: SInt) : SInt = {
    assert(a.getWidth == b.getWidth, "Operands width must match")
    assert(isPow2(a.getWidth), "Operands width must be a power of 2")
    val ab = a * b

    willOverflow(a, b) ? S(a.maxValue) | getX2High(round(ab))
  }

  def willOverflow(a: SInt, b: SInt) : Bool = {
    (a === b) & (a === S(a.minValue))
  }

  def round(ab: SInt) : SInt = {
    val nudge = (ab >= 0) ? S(1 << ((ab.getWidth/2) - 2)) | S(1 - (1 << ((ab.getWidth/2) - 2)))
    ab + nudge
  }

  def getX2High(ab: SInt) : SInt = {
    ab(ab.high - 1 downto (ab.getWidth/2 - 1))
  }
}

