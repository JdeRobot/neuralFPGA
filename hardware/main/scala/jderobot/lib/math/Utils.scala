package jderobot.lib.math

import spinal.core._

/**
  * Correctly-rounded-to-nearest division by a power-of-two.
  */
object RoundingDivideByPOT {
  def apply(x: SInt, exp: UInt): SInt = {
    assert(exp.maxValue < x.getWidth, "exp max value can't be bigger than x width")
    val mask = ((U"1" << exp) -1).asBits
    val remainder = (x.asBits & mask.resized).asUInt
    val threshold = (mask >> 1).asUInt + x.msb.asUInt

    (x |>> exp) + ((remainder > threshold) ? S(1) | S(0)).resized
  }

  def apply(x: SInt, exp: Int): SInt = {
    assert(exp < x.getWidth, "exp can't be bigger than x width")
    val mask = ((U"1" << exp) -1).asBits
    val remainder = (x.asBits & mask.resized).asUInt
    val threshold = (mask >> 1).asUInt + x.msb.asUInt

    (x |>> exp) + ((remainder > threshold) ? S(1) | S(0)).resized
  }
}

/**
  * FIXME: use Multiplier and solve in stages
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

    willOverflow(a, b) ? S(a.maxValue) | RoundingDivideByPOT(ab, (ab.getWidth/2) - 1).subdivideIn(2 slices)(0)
  }

  def willOverflow(a: SInt, b: SInt) : Bool = {
    (a === b) & (a === S(a.minValue))
  }
}

