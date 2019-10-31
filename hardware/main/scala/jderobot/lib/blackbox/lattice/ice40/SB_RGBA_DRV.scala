package jderobot.lib.blackbox.lattice.ice40

import spinal.core._

//ICE40LEDDriverUsageGuide
//SB_RGBA_DRV Attribute Description
//The SB_RGBA_DRV primitive contains the following parameter and their default values:
//Parameter CURRENT_MODE = “0b0”;
//Parameter RGB0_CURRENT = “0b000000”;
//Parameter RGB1_CURRENT = “0b000000”;
//Parameter RGB2_CURRENT = “0b000000”;
//Parameter values:
//“0b0” = Full Current Mode
//“0b1” = Half Current Mode
//“0b000000” = 0mA. // Set this value to use the associated SB_IO_OD instance at RGB LED location.
//“0b000001” = 4 mA for Full Mode; 2 mA for Half Mode
//“0b000011” = 8 mA for Full Mode; 4 mA for Half Mode
//“0b000111” = 12 mA for Full Mode; 6mA for Half Mode
//“0b001111” = 16 mA for Full Mode; 8 mA for Half Mode
//“0b011111” = 20 mA for Full Mode; 10 mA for Half Mode
//“0b111111” = 24 mA for Full Mode; 12 mA for Half Mode

case class SB_RGBA_DRV_Config(currentMode: String = "0b1",
                              rgb0Current: String = "0b000001",
                              rgb1Current: String = "0b000001",
                              rgb2Current: String = "0b000001")

case class SB_RGBA_DRV(config: SB_RGBA_DRV_Config) extends BlackBox {
  val generic = new Generic {
    val CURRENT_MODE = config.currentMode
    val RGB0_CURRENT = config.rgb0Current
    val RGB1_CURRENT = config.rgb1Current
    val RGB2_CURRENT = config.rgb2Current
  }

  val CURREN = in Bool
  val RGBLEDEN = in Bool
  val RGB0PWM = in Bool
  val RGB1PWM = in Bool
  val RGB2PWM = in Bool
  val RGB0 = out Bool
  val RGB1 = out Bool
  val RGB2 = out Bool

  addRTLPath("hardware/main/resources/rtl/lattice/ice40/SB_RGBA_DRV.v") //verilator mockup
}
