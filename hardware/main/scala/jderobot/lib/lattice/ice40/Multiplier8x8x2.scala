package jderobot.lib.lattice.ice40

import jderobot.lib.blackbox.lattice.ice40._
import spinal.core._

case class Multiplier8x8x2(signed: Boolean = false) extends Component {
  val io = new Bundle{
    val a = in Vec(Bits(8 bits), 2)
    val b = in Vec(Bits(8 bits), 2)
    val output = out Vec(Bits(16 bits), 2)
  }

  val mac16 = SB_MAC16(SB_MAC16_Config(
    topOutputSelect = OutputSelectEnum.MUL_8x8,
    bottomOutputSelect = OutputSelectEnum.MUL_8x8,
    mode8x8 = true,
    aSigned = signed,
    bSigned = signed
  ))

  mac16.C := 0
  mac16.A := io.a.asBits
  mac16.B := io.b.asBits
  mac16.D := 0

  mac16.AHOLD := False
  mac16.BHOLD := False
  mac16.CHOLD := False
  mac16.DHOLD := False

  mac16.IRSTTOP := False
  mac16.IRSTBOT := False
  mac16.ORSTTOP := False
  mac16.ORSTBOT := False

  mac16.OLOADTOP := False
  mac16.OLOADBOT := False
  mac16.ADDSUBTOP := False
  mac16.ADDSUBBOT := False
  mac16.OHOLDTOP := False
  mac16.OHOLDBOT := False
  mac16.CI := False
  mac16.ACCUMCI := False
  mac16.SIGNEXTIN := False
  io.output.assignFromBits(mac16.O)
}

case class Multiplier8x8x2Unsigned() extends Component {
  val io = new Bundle{
    val a = in Vec(UInt(8 bits), 2)
    val b = in Vec(UInt(8 bits), 2)
    val output = out Vec(UInt(16 bits), 2)
  }

  val multiplier = Multiplier8x8x2()
  multiplier.io.a.assignFromBits(io.a.asBits)
  multiplier.io.b.assignFromBits(io.b.asBits)
  io.output.assignFromBits(multiplier.io.output.asBits)
}

case class Multiplier8x8x2Signed() extends Component {
  val io = new Bundle{
    val a = in Vec(SInt(8 bits), 2)
    val b = in Vec(SInt(8 bits), 2)
    val output = out Vec(SInt(16 bits), 2)
  }

  val multiplier = Multiplier8x8x2(signed = true)
  multiplier.io.a.assignFromBits(io.a.asBits)
  multiplier.io.b.assignFromBits(io.b.asBits)
  io.output.assignFromBits(multiplier.io.output.asBits)
}