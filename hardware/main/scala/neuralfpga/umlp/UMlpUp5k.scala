package neuralfpga.umlp

import jderobot.lib.blackbox.lattice.ice40.{SB_GB, SB_RGBA_DRV, SB_RGBA_DRV_Config}
import jderobot.lib.lattice.ice40.{Bram, Spram}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._
import spinal.lib.com.jtag.Jtag
import spinal.lib.io.TriStateArray
import vexriscv._
import vexriscv.plugin._

case class UMlpUp5kParameters(clkFrequency : HertzNumber,
                              noComplianceOverhead : Boolean = false,
                              withRfBypass : Boolean = false,
                              withPipelining : Boolean = false,
                              withCsr : Boolean = true,
                              hardwareBreakpointsCount: Int = 2,
                              withJtag : Boolean = false){

  //Create a VexRiscv configuration from the SoC configuration
  def toVexRiscvConfig() = {
    val config = VexRiscvConfig(
      withMemoryStage = true,
      withWriteBackStage = true,
      List(
        new IBusSimplePlugin(
          resetVector = 0x80000000l,
          cmdForkOnSecondStage = false,
          cmdForkPersistence = false,
          prediction = NONE,
          catchAccessFault = false,
          compressedGen = false,
          injectorStage = false,
          rspHoldValue = !withPipelining,
          singleInstructionPipeline = !withPipelining,
          busLatencyMin = 1,
          pendingMax = if(withPipelining) 3 else 1
        ),
        new DBusSimplePlugin(
          catchAddressMisaligned = withCsr && !noComplianceOverhead,
          catchAccessFault = false
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = true,
          x0Init = false,
          readInExecute = true,
          syncUpdateOnStall = withPipelining
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true,
          decodeAddSub = false
        ),
        new LightShifterPlugin(),
        new BranchPlugin(
          earlyBranch = true,
          catchAddressMisaligned = withCsr && !noComplianceOverhead,
          fenceiGenAsAJump = withPipelining,
          fenceiGenAsANop = !withPipelining
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
    if(withPipelining){
      config.plugins += new HazardSimplePlugin(
        bypassExecute = withRfBypass,
        bypassMemory  = withRfBypass,
        bypassWriteBackBuffer = withRfBypass
      )
    } else {
      config.plugins += new NoHazardPlugin
    }
    if(withCsr) config.plugins +=
      new CsrPlugin(CsrPluginConfig.smallest(mtvecInit = 0x80000020l))
    config
  }
}

object UMlpUp5kParameters {
  def default = up5kDefault
  def up5kDefault = UMlpUp5kParameters(clkFrequency = 12 MHz)
}

case class UMlpUp5k(p: UMlpUp5kParameters) extends Component {
  val io = new Bundle {
    val clk, reset = in Bool()
    val leds = out Bits(3 bits)
    val jtag = p.withJtag generate slave(Jtag())
  }

  val resetCtrlClockDomain = ClockDomain(
    clock = io.clk,
    config = ClockDomainConfig(
      resetKind = BOOT //Bitstream loaded FF
    )
  )

  val resetCtrl = new ClockingArea(resetCtrlClockDomain) {
    val resetUnbuffered  = False

    //Power on reset counter
    val resetCounter = Reg(UInt(6 bits)) init(0)
    when(!resetCounter.andR){
      resetCounter := resetCounter + 1
      resetUnbuffered := True
    }
    when(BufferCC(io.reset)){
      resetCounter := 0
    }

    //Create all reset used later in the design
    val systemResetSet = False
    val debugReset = SB_GB(RegNext(resetUnbuffered))
    val systemReset = SB_GB(RegNext(resetUnbuffered || BufferCC(systemResetSet)))
  }

  val debugClockDomain = ClockDomain(
    clock = io.clk,
    reset = resetCtrl.debugReset,
    frequency = FixedFrequency(p.clkFrequency),
    config = ClockDomainConfig(
      resetKind = spinal.core.SYNC
    )
  )

  val systemClockDomain = ClockDomain(
    clock = io.clk,
    reset = resetCtrl.systemReset,
    frequency = FixedFrequency(p.clkFrequency),
    config = ClockDomainConfig(
      resetKind = spinal.core.SYNC
    )
  )

  //There is defined the whole SoC stuff
  val system = new ClockingArea(systemClockDomain) {
    val busConfig = PipelinedMemoryBusConfig(addressWidth = 32, dataWidth = 32)

    //Define the different memory busses and interconnect that will be use in the SoC
    val dBus = PipelinedMemoryBus(busConfig)
    val iBus = PipelinedMemoryBus(busConfig)
    val slowBus = PipelinedMemoryBus(busConfig)
    val interconnect = PipelinedMemoryBusInterconnect()

    //Define slave/peripheral components
    val dRam = Spram()
    val iRam = Bram(onChipRamSize = 16 KiB)
    //iRam.mem.initBigInt(for(i <- 0 until 256) yield BigInt(0x13))

    val peripherals = Peripherals()
    peripherals.io.leds <> io.leds

    //Map the different slave/peripherals into the interconnect
    interconnect.addSlaves(
      iRam.io.bus         -> SizeMapping(0x80000000l,  16 KiB),
      dRam.io.bus         -> SizeMapping(0x80004000l,  64 KiB),
      peripherals.io.bus  -> SizeMapping(0xF0000000l, 256 Byte),
      slowBus             -> DefaultMapping
    )

    //Specify which master bus can access to which slave/peripheral
    interconnect.addMasters(
      dBus   -> List(dRam.io.bus, slowBus),
      iBus   -> List(iRam.io.bus, slowBus),
      slowBus-> List(dRam.io.bus, iRam.io.bus, peripherals.io.bus)
    )

    //Add pipelining to buses connections to get a better maximal frequancy
    interconnect.setConnector(dBus, slowBus){(m, s) =>
      m.cmd.halfPipe() >> s.cmd
      m.rsp            << s.rsp
    }
    interconnect.setConnector(iBus, slowBus){(m, s) =>
      m.cmd.halfPipe() >> s.cmd
      m.rsp            << s.rsp
    }

    interconnect.setConnector(slowBus){(m, s) =>
      m.cmd >> s.cmd
      m.rsp << s.rsp.stage()
    }


    //Map the CPU into the SoC depending the Plugins used
    val cpuConfig = p.toVexRiscvConfig()
    p.withJtag generate cpuConfig.add(new DebugPlugin(debugClockDomain, p.hardwareBreakpointsCount))
    //    io.jtag.flatten.filter(_.isOutput).foreach(_.assignDontCare())

    val cpu = new VexRiscv(cpuConfig)
    for (plugin <- cpu.plugins) plugin match {
      case plugin : IBusSimplePlugin => iBus << plugin.iBus.toPipelinedMemoryBus()
      case plugin : DBusSimplePlugin => dBus << plugin.dBus.toPipelinedMemoryBus()
      case plugin : CsrPlugin => {
        plugin.externalInterrupt := False //Not used
        plugin.timerInterrupt := peripherals.io.mTimeInterrupt
      }
      case plugin : DebugPlugin         => plugin.debugClockDomain{
        resetCtrl.systemResetSet setWhen(RegNext(plugin.io.resetOut))
        io.jtag <> plugin.io.bus.fromJtag()
      }
      case _ =>
    }
  }
}

//Up5kEvn board specific toplevel.
case class UMlpUp5kEvn(p : UMlpUp5kParameters) extends Component{
  val io = new Bundle {
    val clk  = in  Bool()
    val leds = new Bundle {
      val r,g,b = out Bool()
    }
  }

  val clkBuffer = SB_GB()
  clkBuffer.USER_SIGNAL_TO_GLOBAL_BUFFER <> io.clk

  val soc = UMlpUp5k(p)

  soc.io.clk      <> clkBuffer.GLOBAL_BUFFER_OUTPUT
  soc.io.reset    <> False


  val ledDriver = SB_RGBA_DRV(SB_RGBA_DRV_Config(
    currentMode = "0b1", rgb0Current = "0b000001", rgb1Current = "0b000001",rgb2Current = "0b000001"
  ))
  ledDriver.CURREN   := True
  ledDriver.RGBLEDEN := True
  ledDriver.RGB0PWM  := soc.io.leds(0)
  ledDriver.RGB1PWM  := soc.io.leds(1)
  ledDriver.RGB2PWM  := soc.io.leds(2)

  ledDriver.RGB0 <> io.leds.b
  ledDriver.RGB1 <> io.leds.g
  ledDriver.RGB2 <> io.leds.r
}

object UMlpUp5k {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(UMlpUp5k(UMlpUp5kParameters.default))
  }
}

object UMlpUp5kEvn {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(UMlpUp5kEvn(UMlpUp5kParameters.default))
  }
}