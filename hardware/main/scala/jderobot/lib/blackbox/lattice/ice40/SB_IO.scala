package jderobot.lib.blackbox.lattice.ice40

import spinal.core._

case class SB_IO(pinType : String) extends BlackBox{
  addGeneric("PIN_TYPE", B(pinType))
  val PACKAGE_PIN = inout(Analog(Bool))
  val CLOCK_ENABLE = in Bool() default(False)
  val INPUT_CLK = in Bool() default(False)
  val OUTPUT_CLK = in Bool() default(False)
  val OUTPUT_ENABLE = in Bool() default(False)
  val D_OUT_0 = in Bool() default(False)
  val D_OUT_1 = in Bool() default(False)
  val D_IN_0 = out Bool()
  val D_IN_1 = out Bool()
  setDefinitionName("SB_IO")
}

case class SB_IO_SCLK() extends BlackBox{
  addGeneric("PIN_TYPE", B"010000")
  val PACKAGE_PIN = out Bool()
  val OUTPUT_CLK = in Bool()
  val CLOCK_ENABLE = in Bool()
  val D_OUT_0 = in Bool()
  val D_OUT_1 = in Bool()
  setDefinitionName("SB_IO")
}

case class SB_IO_DATA() extends BlackBox{
  addGeneric("PIN_TYPE", B"110000")
  val PACKAGE_PIN = inout(Analog(Bool))
  val CLOCK_ENABLE = in Bool()
  val INPUT_CLK = in Bool()
  val OUTPUT_CLK = in Bool()
  val OUTPUT_ENABLE = in Bool()
  val D_OUT_0 = in Bool()
  val D_OUT_1 = in Bool()
  val D_IN_0 = out Bool()
  val D_IN_1 = out Bool()
  setDefinitionName("SB_IO")
}