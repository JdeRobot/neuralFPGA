package jderobot.lib.accelerator

import jderobot.lib.lattice.ice40._
import spinal.core._
import spinal.lib._

case class Mac8x9Generics(accumulatorWidth: Int = 32,
                          useHwMultiplier: Boolean = false)

case class Mac8x9Cmd(config: Mac8x9Generics) extends Bundle {
  val x = Vec(UInt(8 bits), 9)
  val y = Vec(UInt(8 bits), 9)
  val acc0 = UInt(config.accumulatorWidth bits)
}

case class Mac8x9Rsp(config: Mac8x9Generics) extends Bundle {
  val acc = UInt(config.accumulatorWidth bits)
}

case class Mac8x9StageContext(config: Mac8x9Generics, sumBits: Int, sumSize: Int) extends Bundle {
  val sums = Vec(UInt(sumBits bits), sumSize)
  val acc0 = UInt(config.accumulatorWidth bits)
}

case class Mac8x9(config: Mac8x9Generics) extends Component {
  val io = new Bundle {
    val cmd = slave Stream(Mac8x9Cmd(config))
    val rsp = master Stream(Mac8x9Rsp(config))
  }

  val multStage = new Area {
    val input = io.cmd
    val output = Stream(Mac8x9StageContext(config, 16, 9))

    output.translateFrom(input)((to, from) => {
      if (config.useHwMultiplier) {
        for( i <- 0 until 4) {
          val multiplier = Multiplier8x8x2Unsigned()

          multiplier.io.a := Vec(from.x(i * 2), from.x(i * 2 + 1))
          multiplier.io.b := Vec(from.y(i * 2), from.y(i * 2 + 1))

          to.sums(i * 2) := multiplier.io.output(0)
          to.sums(i * 2 + 1) := multiplier.io.output(1)
        }
        to.sums(8) := from.x(8) * from.y(8)
      } else {
        to.sums := Vec((from.x,from.y).zipped.map(_ * _))
      }
      to.acc0 := from.acc0
    })
  }

  val adderTree = AdderTreex9(AdderTreex9Generics(inputWidth = 16, accumulatorWidth = 32))
  adderTree.io.cmd.translateFrom(multStage.output.stage())((to, from) => {
    to.x := from.sums
    to.acc0 := from.acc0
  })

  io.rsp.translateFrom(adderTree.io.rsp)((to, from) => {
    to.acc := from.acc
  })
}

object Mac8x9 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(Mac8x9(Mac8x9Generics(useHwMultiplier = true)))
  }
}