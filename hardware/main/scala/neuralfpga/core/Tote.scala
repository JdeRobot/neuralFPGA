package neuralfpga.core

import jderobot.lib.lattice.ice40.{Bram, Spram}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._
import spinal.lib.com.jtag.Jtag
import spinal.lib.io.{Gpio, TriStateArray}
import vexriscv._
import vexriscv.plugin._

case class ToteParameters(clkFrequency : HertzNumber,
                          gpioA : Gpio.Parameter,
                          hardwareBreakpointsCount : Int,
                          withJtag : Boolean){
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
          compressedGen = true
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
        new MulDivIterativePlugin(
          genMul = false
        ),
        new MulSimplePlugin(),
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
    clkFrequency = 12 MHz,
    gpioA = Gpio.Parameter(
      width = 8
    ),
    hardwareBreakpointsCount = 2,
    withJtag = true
  )
}

case class Tote(p: ToteParameters) extends Component {
  val io = new Bundle {
    //Clocks / reset
    val clk, reset = in Bool()

    //Main components IO
    val jtag = p.withJtag generate slave(Jtag())

    //Peripherals IO
    val gpioA = master(TriStateArray(p.gpioA.width bits))
  }

  val resetCtrlClockDomain = ClockDomain(
    clock = io.clk,
    config = ClockDomainConfig(
      resetKind = spinal.core.BOOT
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
    val debugReset = RegNext(resetUnbuffered)
    val systemReset = RegNext(resetUnbuffered || BufferCC(systemResetSet))
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
    //interconnect
    val interconnect = PipelinedMemoryBusInterconnect()

    //busses
    val busConfig = PipelinedMemoryBusConfig(addressWidth = 32, dataWidth = 32)
    val dBus = PipelinedMemoryBus(busConfig)
    val iBus = PipelinedMemoryBus(busConfig)
    val mainBus = PipelinedMemoryBus(busConfig)


    //Define slave/peripheral components
    val iRam = Spram()
    val dRam = Spram()

    val gpioCtrl = PipelinedMemoryGpio(p.gpioA)
    gpioCtrl.io.gpio <> io.gpioA

    val machineTimer = MachineTimer()

    //Map the different slave/peripherals into the interconnect
    interconnect.addSlaves(
      iRam.io.bus          -> SizeMapping(0x80000000l, 64 KiB),
      dRam.io.bus          -> SizeMapping(0x90000000l, 64 KiB),
      gpioCtrl.io.bus     -> SizeMapping(0xA0000000l,  4 KiB),
      machineTimer.io.bus -> SizeMapping(0xA0001000l,  4 KiB),
      mainBus             -> DefaultMapping
    )

    //Specify which master bus can access to which slave/peripheral
    interconnect.addMasters(
      iBus   -> List(iRam.io.bus, mainBus),
      dBus   -> List(dRam.io.bus, mainBus),
      mainBus-> List(iRam.io.bus, dRam.io.bus, gpioCtrl.io.bus, machineTimer.io.bus)
    )

    //Add pipelining to busses connections to get a better maximal frequency
    interconnect.setConnector(dBus, mainBus){(m, s) =>
      m.cmd.halfPipe() >> s.cmd
      m.rsp            << s.rsp
    }
    interconnect.setConnector(iBus, mainBus){(m, s) =>
      m.cmd.halfPipe() >> s.cmd
      m.rsp            << s.rsp
    }

    interconnect.setConnector(mainBus){(m, s) =>
      m.cmd >> s.cmd
      m.rsp << s.rsp.stage()
    }

    //Map the CPU into the SoC depending the Plugins used
    val cpuConfig = p.toVexRiscvConfig()
    p.withJtag generate cpuConfig.add(new DebugPlugin(debugClockDomain, p.hardwareBreakpointsCount))

    val cpu = new VexRiscv(cpuConfig)
    for (plugin <- cpu.plugins) plugin match {
      case plugin : IBusSimplePlugin => iBus << plugin.iBus.toPipelinedMemoryBus()
      case plugin : DBusSimplePlugin => dBus << plugin.dBus.toPipelinedMemoryBus()
      case plugin : CsrPlugin => {
        plugin.externalInterrupt := False //Not used
        plugin.timerInterrupt := machineTimer.io.mTimeInterrupt
      }
      case plugin : DebugPlugin => plugin.debugClockDomain{
        resetCtrl.systemResetSet setWhen RegNext(plugin.io.resetOut)
        io.jtag <> plugin.io.bus.fromJtag()
      }
      case _ =>
    }
  }
}

//Up5kEvn board specific toplevel.
//case class ToteUp5kEvn(p : ToteParameters) extends Component{
//  val io = new Bundle {
//    val clk  = in  Bool()
//    val leds = new Bundle {
//      val r,g,b = out Bool()
//    }
//  }
//
//  val clkBuffer = SB_GB()
//  clkBuffer.USER_SIGNAL_TO_GLOBAL_BUFFER <> io.clk
//
//  val soc = Tote(p)
//
//  soc.io.clk      <> clkBuffer.GLOBAL_BUFFER_OUTPUT
//  soc.io.reset    <> False
//
//
//  val ledDriver = SB_RGBA_DRV(SB_RGBA_DRV_Config(
//    currentMode = "0b1", rgb0Current = "0b000001", rgb1Current = "0b000001",rgb2Current = "0b000001"
//  ))
//  ledDriver.CURREN   := True
//  ledDriver.RGBLEDEN := True
//  ledDriver.RGB0PWM  := soc.io.gpioA(0)
//  ledDriver.RGB1PWM  := soc.io.leds(1)
//  ledDriver.RGB2PWM  := soc.io.leds(2)
//
//  ledDriver.RGB0 <> io.leds.b
//  ledDriver.RGB1 <> io.leds.g
//  ledDriver.RGB2 <> io.leds.r
//}

object Tote {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog({
      val toplevel = Tote(ToteParameters.default)
      toplevel.system.dBus.addAttribute(Verilator.public)
      toplevel
    })
  }
}

//object ToteUp5kEvn {
//  def main(args: Array[String]) {
//    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
//    SpinalConfig(
//      targetDirectory = outRtlDir
//    ).generateVerilog(ToteUp5kEvn(ToteParameters.default))
//  }
//}