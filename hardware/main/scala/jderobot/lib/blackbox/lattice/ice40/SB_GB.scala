package jderobot.lib.blackbox.lattice.ice40

import spinal.core._

case class SB_GB() extends BlackBox{
  val USER_SIGNAL_TO_GLOBAL_BUFFER = in Bool()
  val GLOBAL_BUFFER_OUTPUT = out Bool()

  addRTLPath("src/main/resources/rtl/lattice/ice40/SB_GB.v") //verilator mockup
}

object SB_GB{
  def apply(input : Bool) : Bool = {
    val c = SB_GB().setCompositeName(input, "SB_GB", true)
    c.USER_SIGNAL_TO_GLOBAL_BUFFER := input
    c.GLOBAL_BUFFER_OUTPUT
  }
}
