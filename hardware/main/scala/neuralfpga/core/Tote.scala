package neuralfpga.core

import jderobot.lib.accelerator._
import jderobot.lib.blackbox.lattice.ice40._
import jderobot.lib.io.PipelinedMemoryGpio
import jderobot.lib.lattice.ice40._
import jderobot.lib.misc.MachineTimer
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.simple._
import spinal.lib.com.jtag.Jtag
import spinal.lib.io.{Gpio, InOutWrapper, TriState, TriStateArray}
import vexriscv._
import vexriscv.plugin._

case class ToteParameters(clkFrequency : HertzNumber,
                          gpioA : Gpio.Parameter,
                          iRamOnChipRamSize: BigInt = 128 KiB,
                          dRamOnChipRamSize: BigInt = 128 KiB,
                          hardwareBreakpointsCount : Int = 0,
                          withJtag : Boolean = false){
  //Create a VexRiscv configuration from the SoC configuration
  def toVexRiscvConfigSmallAndProductive() = {
    //similar to vexriscv.demo.GenSmallAndProductive
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
        new CsrPlugin(CsrPluginConfig.all),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new MulDivIterativePlugin(),
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new LightShifterPlugin(),
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true
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
  def default = ToteParameters(
    clkFrequency = 12 MHz,
    gpioA = Gpio.Parameter(
      width = 8
    )
  )
  def jtag = ToteParameters(
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
    val iRam = Bram(onChipRamSize = p.iRamOnChipRamSize)
    val dRam = Bram(onChipRamSize = p.dRamOnChipRamSize)

    val gpioCtrl = PipelinedMemoryGpio(p.gpioA)
    gpioCtrl.io.gpio <> io.gpioA

    //val machineTimer = MachineTimer()
    val accelerator = PipelinedMemoryAcceleratorV1Ctlr(AcceleratorV1Generics(rowBufferConfig = WindowBuffer3x3Generics()))

    //Map the different slave/peripherals into the interconnect
    interconnect.addSlaves(
      iRam.io.bus         -> SizeMapping(0x80000000L, p.iRamOnChipRamSize),
      dRam.io.bus         -> SizeMapping(0x90000000L, p.dRamOnChipRamSize),
      gpioCtrl.io.bus     -> SizeMapping(0xA0000000L,  4 KiB),
      //machineTimer.io.bus -> SizeMapping(0xA0001000L,  4 KiB),
      accelerator.io.bus  -> SizeMapping(0xB0000000L,  4 KiB),
      mainBus             -> DefaultMapping
    )

    //Specify which master bus can access to which slave/peripheral
    interconnect.addMasters(
      iBus   -> List(iRam.io.bus, mainBus),
      dBus   -> List(dRam.io.bus, mainBus),
      //mainBus-> List(iRam.io.bus, dRam.io.bus, gpioCtrl.io.bus, machineTimer.io.bus, accelerator.io.bus)
      mainBus-> List(iRam.io.bus, dRam.io.bus, gpioCtrl.io.bus, accelerator.io.bus)
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
    val cpuConfig = p.toVexRiscvConfigSmallAndProductive()
    p.withJtag generate cpuConfig.add(new DebugPlugin(debugClockDomain, p.hardwareBreakpointsCount))

    val cpu = new VexRiscv(cpuConfig)
    for (plugin <- cpu.plugins) plugin match {
      case plugin : IBusSimplePlugin => iBus << plugin.iBus.toPipelinedMemoryBus()
      case plugin : DBusSimplePlugin => dBus << plugin.dBus.toPipelinedMemoryBus()
      case plugin : CsrPlugin => {
        plugin.externalInterrupt := False //Not used
        plugin.timerInterrupt := False //machineTimer.io.mTimeInterrupt
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
case class ToteUp5kEvn(p : ToteParameters) extends Component{
  val io = new Bundle {
    val ICE_CLK  = in  Bool()

    //Gpio
    val IOT_37A = inout(Analog(Bool))
    val IOT_36B = inout(Analog(Bool))
    val IOT_44B = inout(Analog(Bool))
    val IOT_49A = inout(Analog(Bool))
    val IOB_22A = inout(Analog(Bool))
    val IOB_23B = inout(Analog(Bool))
    val IOB_24A = inout(Analog(Bool))
    val IOB_25B = inout(Analog(Bool))

    //JTAG
    val IOB_29B = in  Bool()
    val IOB_31B = in  Bool()
    val IOB_20A = out Bool()
    val IOB_18A = in  Bool()

    val LED_BLUE  = out Bool()
    val LED_GREEN = out Bool()
    val LED_RED   = out Bool()
  }
  noIoPrefix()

  val clkBuffer = SB_GB()
  clkBuffer.USER_SIGNAL_TO_GLOBAL_BUFFER <> io.ICE_CLK

  val soc = Tote(p)

  soc.io.clk      <> clkBuffer.GLOBAL_BUFFER_OUTPUT
  soc.io.reset    <> False

  def ioSbComb(io : Bool, design : TriState[Bool]): Unit ={
    val bb = SB_IO("101001").setCompositeName(io, "SB")
    bb.PACKAGE_PIN <> io
    bb.D_IN_0 <> design.read
    bb.D_OUT_0 <> design.write
    bb.OUTPUT_ENABLE <> design.writeEnable
  }

  ioSbComb(io.IOT_37A, soc.io.gpioA(0))
  ioSbComb(io.IOT_36B, soc.io.gpioA(1))
  ioSbComb(io.IOT_44B, soc.io.gpioA(2))
  ioSbComb(io.IOT_49A, soc.io.gpioA(3))
  ioSbComb(io.IOB_22A, soc.io.gpioA(4))
  ioSbComb(io.IOB_23B, soc.io.gpioA(5))
  ioSbComb(io.IOB_24A, soc.io.gpioA(6))
  ioSbComb(io.IOB_25B, soc.io.gpioA(7))
  //  soc.io.gpioA.read := io.IOT_49A ## io.IOT_44B ## io.IOT_36B ## io.IOT_37A ## B"0000"

  if(p.withJtag) {
    soc.io.jtag.tms <> io.IOB_29B
    soc.io.jtag.tdi <> io.IOB_31B
    soc.io.jtag.tdo <> io.IOB_20A
    soc.io.jtag.tck <> io.IOB_18A
  } else {
    io.IOB_20A := False
  }

  val ledDriver = SB_RGBA_DRV(SB_RGBA_DRV_Config())
  ledDriver.CURREN   := True
  ledDriver.RGBLEDEN := True
  ledDriver.RGB0PWM  := soc.io.gpioA.write(0)
  ledDriver.RGB1PWM  := soc.io.gpioA.write(1)
  ledDriver.RGB2PWM  := soc.io.gpioA.write(2)

  ledDriver.RGB0 <> io.LED_BLUE
  ledDriver.RGB1 <> io.LED_GREEN
  ledDriver.RGB2 <> io.LED_RED
}

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

object ToteJtag {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog({
      val toplevel = Tote(ToteParameters.jtag)
      toplevel.system.dBus.addAttribute(Verilator.public)
      toplevel
    })
  }
}

object ToteUp5kEvn {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(InOutWrapper(ToteUp5kEvn(ToteParameters.jtag)))
  }
}