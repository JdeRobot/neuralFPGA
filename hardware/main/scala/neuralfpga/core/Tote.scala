package neuralfpga.core

import jderobot.lib.blackbox.lattice.ice40.{SB_GB, SB_RGBA_DRV, SB_RGBA_DRV_Config}
import jderobot.lib.lattice.ice40.{Bram, Spram}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._
import vexriscv._
import vexriscv.plugin._

case class ToteParameters(clkFrequency : HertzNumber){
  //Create a VexRiscv configuration from the SoC configuration
  def toVexRiscvConfig() = {
    //from vexriscv.demo.GenSmallAndProductive
    val config = VexRiscvConfig(
      withMemoryStage = true,
      withWriteBackStage = true,
      plugins = List(
        new IBusSimplePlugin(
          resetVector = 0x80000000l,
          cmdForkOnSecondStage = false,
          cmdForkPersistence = false,
          prediction = NONE,
          catchAccessFault = false,
          compressedGen = false
        ),
        new DBusSimplePlugin(
          catchAddressMisaligned = false,
          catchAccessFault = false
        ),
        new CsrPlugin(CsrPluginConfig.smallest),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = false
        ),
        new LightShifterPlugin(),
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = false
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
    config
  }
}

object ToteParameters {
  def default = up5kDefault
  def up5kDefault = ToteParameters(
    clkFrequency = 12 MHz
  )
}

case class Tote(p: ToteParameters) extends Component {
  val io = new Bundle {
    val clk, reset = in Bool()
    val leds = out Bits(3 bits)
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
    val systemReset = SB_GB(RegNext(resetUnbuffered || BufferCC(systemResetSet)))
  }

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
    //interconnect
    val interconnect = PipelinedMemoryBusInterconnect()

    //busses
    val busConfig = PipelinedMemoryBusConfig(addressWidth = 32, dataWidth = 32)
    val dBus = PipelinedMemoryBus(busConfig)
    val iBus = PipelinedMemoryBus(busConfig)
    val slowBus = PipelinedMemoryBus(busConfig)


    //Define slave/peripheral components
    val dRam = Spram()
    val iRam = Bram(onChipRamSize = 8 KiB)
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

    val cpu = new VexRiscv(cpuConfig)
    for (plugin <- cpu.plugins) plugin match {
      case plugin : IBusSimplePlugin => iBus << plugin.iBus.toPipelinedMemoryBus()
      case plugin : DBusSimplePlugin => dBus << plugin.dBus.toPipelinedMemoryBus()
      case plugin : CsrPlugin => {
        plugin.externalInterrupt := False //Not used
        plugin.timerInterrupt := peripherals.io.mTimeInterrupt
      }
      case _ =>
    }
  }
}

//Up5kEvn board specific toplevel.
case class ToteUp5kEvn(p : ToteParameters) extends Component{
  val io = new Bundle {
    val clk  = in  Bool()
    val leds = new Bundle {
      val r,g,b = out Bool()
    }
  }

  val clkBuffer = SB_GB()
  clkBuffer.USER_SIGNAL_TO_GLOBAL_BUFFER <> io.clk

  val soc = Tote(p)

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

object Tote {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(Tote(ToteParameters.default))
  }
}

object ToteUp5kEvn {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(ToteUp5kEvn(ToteParameters.default))
  }
}