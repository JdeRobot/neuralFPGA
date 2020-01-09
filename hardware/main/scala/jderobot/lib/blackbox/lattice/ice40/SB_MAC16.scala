package jderobot.lib.blackbox.lattice.ice40

import spinal.core._

object OutputSelectEnum extends Enumeration {
  val ADD_SUB_NOT_REGISTERED, ADD_SUB_REGISTERED, MUL_8x8, MUL_16x16 = Value
}

object TopAddSubLowerInputEnum extends Enumeration {
  val INPUT_A, MUL_8x8_OUTPUT_TOP, MUL_16x16_UPPER_16_OUTPUTS, SIGN_EXT_FROM_Z15 = Value
}

object TopAddSubUpperInputEnum extends Enumeration {
  val ADD_SUB_OUTPUT_REG, INPUT_C = Value
}

object BottomAddSubLowerInputEnum extends Enumeration {
  val INPUT_B, MUL_8x8_OUTPUT_TOP, MUL_16x16_UPPER_16_OUTPUTS, SIGNEXTIN = Value
}

object BottomAddSubUpperInputEnum extends Enumeration {
  val ADD_SUB_OUTPUT_REG, INPUT_D = Value
}

object AddSubCarrySelectEnum extends Enumeration {
  val CONSTANT_0, CONSTANT_1, CASCADE_ACCUMOUT, CASCADE_CO = Value
}

case class SB_MAC16_Config(negTrigger: Boolean = false,
                           cReg: Boolean = false,
                           aReg: Boolean = false,
                           bReg: Boolean = false,
                           dReg: Boolean = false,
                           top8x8MultReg: Boolean = false,
                           bottom8x8MultReg: Boolean = false,
                           pipeline16x16MultReg1: Boolean = false,
                           pipeline16x16MultReg2: Boolean = false,
                           topOutputSelect: OutputSelectEnum.Value = OutputSelectEnum.ADD_SUB_NOT_REGISTERED,
                           topAddSubLowerInput: TopAddSubLowerInputEnum.Value = TopAddSubLowerInputEnum.INPUT_A,
                           topAddSubUpperInput: TopAddSubUpperInputEnum.Value = TopAddSubUpperInputEnum.ADD_SUB_OUTPUT_REG,
                           topAddSubCarrySelect: AddSubCarrySelectEnum.Value = AddSubCarrySelectEnum.CONSTANT_0,
                           bottomOutputSelect: OutputSelectEnum.Value = OutputSelectEnum.ADD_SUB_NOT_REGISTERED,
                           bottomAddSubLowerInput: BottomAddSubLowerInputEnum.Value = BottomAddSubLowerInputEnum.INPUT_B,
                           bottomAddSubUpperInput: BottomAddSubUpperInputEnum.Value = BottomAddSubUpperInputEnum.ADD_SUB_OUTPUT_REG,
                           bottomAddSubCarrySelect: AddSubCarrySelectEnum.Value = AddSubCarrySelectEnum.CONSTANT_0,
                           mode8x8: Boolean = false,
                           aSigned: Boolean = false,
                           bSigned: Boolean = false)

case class SB_MAC16(config: SB_MAC16_Config) extends BlackBox {
  addGeneric("NEG_TRIGGER", Bool(config.negTrigger))
  addGeneric("C_REG", Bool(config.cReg))
  addGeneric("A_REG", Bool(config.aReg))
  addGeneric("B_REG", Bool(config.bReg))
  addGeneric("D_REG", Bool(config.dReg))
  addGeneric("TOP_8x8_MULT_REG", Bool(config.top8x8MultReg))
  addGeneric("BOT_8x8_MULT_REG", Bool(config.bottom8x8MultReg))
  addGeneric("PIPELINE_16x16_MULT_REG1", Bool(config.pipeline16x16MultReg1))
  addGeneric("PIPELINE_16x16_MULT_REG2", Bool(config.pipeline16x16MultReg2))
  addGeneric("TOPOUTPUT_SELECT", spinal.core.B(config.topOutputSelect.id, 2 bits))
  addGeneric("TOPADDSUB_LOWERINPUT", spinal.core.B(config.topAddSubLowerInput.id, 2 bits))
  addGeneric("TOPADDSUB_UPPERINPUT", spinal.core.B(config.topAddSubUpperInput.id, 1 bits))
  addGeneric("TOPADDSUB_CARRYSELECT", spinal.core.B(config.topAddSubCarrySelect.id, 2 bits))
  addGeneric("BOTOUTPUT_SELECT", spinal.core.B(config.bottomOutputSelect.id, 2 bits))
  addGeneric("BOTADDSUB_LOWERINPUT", spinal.core.B(config.bottomAddSubLowerInput.id, 2 bits))
  addGeneric("BOTADDSUB_UPPERINPUT", spinal.core.B(config.bottomAddSubUpperInput.id, 1 bits))
  addGeneric("BOTADDSUB_CARRYSELECT", spinal.core.B(config.bottomAddSubCarrySelect.id, 2 bits))
  addGeneric("MODE_8x8", Bool(config.mode8x8))
  addGeneric("A_SIGNED", Bool(config.aSigned))
  addGeneric("B_SIGNED", Bool(config.bSigned))

  val CLK = in Bool()
  val CE = in Bool()

  val C = in Bits(16 bits)
  val A = in Bits(16 bits)
  val B = in Bits(16 bits)
  val D = in Bits(16 bits)

  val CHOLD = in Bool()
  val AHOLD = in Bool()
  val BHOLD = in Bool()
  val DHOLD = in Bool()

  val IRSTTOP = in Bool()
  val IRSTBOT = in Bool()
  val ORSTTOP = in Bool()
  val ORSTBOT = in Bool()

  val OLOADTOP = in Bool()
  val OLOADBOT = in Bool()
  val ADDSUBTOP = in Bool()
  val ADDSUBBOT = in Bool()
  val OHOLDTOP = in Bool()
  val OHOLDBOT = in Bool()
  val CI = in Bool()
  val ACCUMCI = in Bool()
  val SIGNEXTIN = in Bool()

  val O = out Bits(32 bits)
  val CO = out Bool()
  val ACCUMCO = out Bool()
  val SIGNEXTOUT = out Bool()

  mapCurrentClockDomain(CLK, null, CE)

  addRTLPath("hardware/main/resources/rtl/lattice/ice40/SB_MAC16.v") //verilator mockup
}
