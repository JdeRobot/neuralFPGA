package umlp

import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusConfig}


case class UMlpConfig(ramConfig : PipelinedMemoryBusConfig)

case class UMlp(config: UMlpConfig) extends Component {
  val io = new Bundle {
    val clk    = in Bool

    val memBus = master(PipelinedMemoryBus(config.ramConfig.addressWidth, config.ramConfig.dataWidth))
  }

  val core = new Area {
    val counter = Reg(UInt(config.ramConfig.addressWidth bits)) init(0)
    val data = Reg(Bits(config.ramConfig.dataWidth bits)) init(0)

    io.memBus.cmd.valid := True
    io.memBus.cmd.address := counter
    io.memBus.cmd.write := False
    io.memBus.cmd.data.assignDontCare()
    io.memBus.cmd.mask.assignDontCare()

    when (io.memBus.cmd.fire) {
      counter := counter + 1
    }

    when (io.memBus.rsp.fire) {
      data := io.memBus.rsp.data
    }
  }
}

object UMlp {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "."
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultClockDomainFrequency = FixedFrequency(12 MHz),
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(UMlp(UMlpConfig(PipelinedMemoryBusConfig(32, 32))))
  }
}