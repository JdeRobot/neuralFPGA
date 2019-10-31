package jderobot.lib.accelerator

import jderobot.lib.blackbox.lattice.ice40._
import jderobot.lib.lattice.ice40._
import spinal.core._
import spinal.lib._

case class Mac8x16Config(accumulatorWidth: Int = 32,
                         useMac16: Boolean = false)

case class Mac8x16Cmd(config: Mac8x16Config) extends Bundle {
  val x = Vec(UInt(8 bits), 16)
  val y = Vec(UInt(8 bits), 16)
  val acc0 = UInt(config.accumulatorWidth bits)
}

case class Mac8x16Rsp(config: Mac8x16Config) extends Bundle {
  val acc = UInt(config.accumulatorWidth bits)
}

case class Mac8x16StageContext(config: Mac8x16Config, level: Int) extends Bundle {
  val sums = Vec(UInt((16 + level) bits), 16 >> level )
  val acc0 = UInt(config.accumulatorWidth bits)
}

case class Mac8x16(config: Mac8x16Config) extends Component {
  val io = new Bundle {
    val cmd = slave Stream(Mac8x16Cmd(config))
    val rsp = master Stream(Mac8x16Rsp(config))
  }

  val multStage = new Area {
    val input = io.cmd
    val output = Stream(Mac8x16StageContext(config, 0))

    output.translateFrom(input)((to, from) => {
      if (config.useMac16) {
        for( i <- 0 until 8) {
          val multiplier = Multiplier8x8x2Unsigned()

          multiplier.io.a := Vec(from.x(i * 2), from.x(i * 2 + 1))
          multiplier.io.b := Vec(from.y(i * 2), from.y(i * 2 + 1))

          to.sums(i * 2) := multiplier.io.output(0)
          to.sums(i * 2 + 1) := multiplier.io.output(1)
        }
      } else {
        to.sums := Vec((from.x,from.y).zipped.map(_ * _))
      }
      to.acc0 := from.acc0
    })
  }

  val addState1 = new Area {
    val input = multStage.output.stage()
    val output = Stream(Mac8x16StageContext(config, 1))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(_.reduce((a, b) => a.resize(a.getWidth + 1) + b)))
      to.acc0 := from.acc0
    })
  }

  val addState2 = new Area {
    val input = addState1.output.stage()
    val output = Stream(Mac8x16StageContext(config, 2))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(_.reduce((a, b) => a.resize(a.getWidth + 1) + b)))
      to.acc0 := from.acc0
    })
  }

  val addState3 = new Area {
    val input = addState2.output.stage()
    val output = Stream(Mac8x16StageContext(config, 3))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(_.reduce((a, b) => a.resize(a.getWidth + 1) + b)))
      to.acc0 := from.acc0
    })
  }

  val addState4 = new Area {
    val input = addState3.output.stage()
    val output = Stream(Mac8x16StageContext(config, 4))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(_.reduce((a, b) => a.resize(a.getWidth + 1) + b)))
      to.acc0 := from.acc0
    })
  }

  val rspStage = new Area {
    val input = addState4.output.stage()

    io.rsp.translateFrom(input)((to, from) => {
      to.acc := from.acc0 + from.sums(0)
    })
  }
}

object Mac8x16 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(Mac8x16(Mac8x16Config(useMac16 = true)))
  }
}

case class UseMac8x16() extends Component {
  val io = new Bundle{
    val rgb0,rgb1,rgb2 = out Bool()
  }
  val x = Reg(Bits(16*8 bits)) init(1)
  val y = Reg(Bits(16*8 bits)) init(1)
  val mac = Mac8x16(Mac8x16Config())
  val state = RegNextWhen(mac.io.rsp.payload.acc, mac.io.rsp.valid)
  mac.io.cmd.valid := True
  mac.io.rsp.ready := True
  mac.io.cmd.payload.x.assignFromBits(x)
  mac.io.cmd.payload.y.assignFromBits(y)
  mac.io.cmd.payload.acc0 := 0
  x := x |<< 1
  y := y |<< 1


  val rgbaDriverConfig = SB_RGBA_DRV_Config(
    currentMode = "0b1",
    rgb0Current = "0b000011",
    rgb1Current = "0b000011",
    rgb2Current = "0b000011"
  )
  val rgbaDriver = SB_RGBA_DRV(rgbaDriverConfig)

  rgbaDriver.CURREN := True
  rgbaDriver.RGBLEDEN := state.subdivideIn(4 slices)(0).orR
  rgbaDriver.RGB0PWM := state.subdivideIn(4 slices)(1).orR
  rgbaDriver.RGB1PWM := state.subdivideIn(4 slices)(2).orR
  rgbaDriver.RGB2PWM := state.subdivideIn(4 slices)(3).orR
  rgbaDriver.RGB0 <> io.rgb0
  rgbaDriver.RGB1 <> io.rgb1
  rgbaDriver.RGB2 <> io.rgb2

  noIoPrefix()
}

object UseMac8x16 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultClockDomainFrequency = FixedFrequency(12 MHz),
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(UseMac8x16())
  }
}