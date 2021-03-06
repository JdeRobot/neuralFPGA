package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._

case class AdderTreex9Generics(inputWidth: Int,
                               accumulatorWidth: Int)

case class AdderTreex9Cmd(config: AdderTreex9Generics) extends Bundle {
  val x = Vec(SInt(config.inputWidth bits), 9)
  val acc0 = SInt(config.accumulatorWidth bits)
}

case class AdderTreex9Rsp(config: AdderTreex9Generics) extends Bundle {
  val acc = SInt(config.accumulatorWidth bits)
}

case class AdderTreex9StageContext(config: AdderTreex9Generics, sumBits: Int, sumSize: Int) extends Bundle {
  val sums = Vec(SInt(sumBits bits), sumSize)
  val acc0 = SInt(config.accumulatorWidth bits)
}

case class AdderTreex9(config: AdderTreex9Generics) extends Component {
  val io = new Bundle {
    val cmd = slave Stream(AdderTreex9Cmd(config))
    val rsp = master Stream(AdderTreex9Rsp(config))
  }

  def add(seq: IndexedSeq[SInt]): SInt = seq match {
    case IndexedSeq(a,b) => a.resize(a.getWidth + 1) + b
    case IndexedSeq(a) => a.resized
  }

  val addState1 = new Area {
    val input = io.cmd
    val output = Stream(AdderTreex9StageContext(config, config.inputWidth + 1, 5))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.x.grouped(2).map(add))
      to.acc0 := from.acc0
    })
  }

  val addState2 = new Area {
    val input = addState1.output.stage()
    val output = Stream(AdderTreex9StageContext(config, config.inputWidth + 2, 3))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(add))
      to.acc0 := from.acc0
    })
  }

  val addState3 = new Area {
    val input = addState2.output.stage()
    val output = Stream(AdderTreex9StageContext(config, config.inputWidth + 3, 2))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(add))
      to.acc0 := from.acc0
    })
  }

  val addState4 = new Area {
    val input = addState3.output.stage()
    val output = Stream(AdderTreex9StageContext(config, config.inputWidth + 4, 1))

    output.translateFrom(input)((to, from) => {
      to.sums := Vec(from.sums.grouped(2).map(add))
      to.acc0 := from.acc0
    })
  }

  val rspStage = new Area {
    val input = addState4.output.stage()

    io.rsp.translateFrom(input)((to, from) => {
      to.acc := from.acc0 + from.sums(0)
    })
//    io.rsp.valid := input.valid && io.cmd.valid
//    io.rsp.acc := input.acc0 + input.sums(0)
//    input.ready := io.rsp.ready

  }
}

object AdderTreex9 {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(AdderTreex9(AdderTreex9Generics(inputWidth = 8, accumulatorWidth = 32)))
  }
}