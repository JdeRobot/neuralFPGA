package jderobot.lib.blackbox.lattice.ice40

import spinal.core._

case class SB_SPRAM256KA() extends BlackBox {
  val ADDRESS = in UInt(14 bits)
  val DATAIN = in Bits(16 bits)
  val MASKWREN = in Bits(4 bits)
  val WREN = in Bool()
  val CHIPSELECT = in Bool()
  val CLOCK = in Bool()
  val STANDBY = in Bool()
  val SLEEP = in Bool()
  val POWEROFF = in Bool()
  val DATAOUT = out Bits(16 bits)

  mapCurrentClockDomain(CLOCK)

  addRTLPath("hardware/main/resources/rtl/lattice/ice40/SB_SPRAM256KA.v")
}
