package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.{BusSlaveFactory, BusSlaveFactoryAddressWrapper, SizeMapping}
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusSlaveFactory}
import spinal.lib.fsm.{EntryPoint, State, StateMachine}

case class AcceleratorV1Generics(xFifoDepth: Int = 512,
                                 yMemDepth: Int = 256,
                                 zFifoDepth: Int = 2048,
                                 rowBufferConfig: WindowBuffer3x3Generics = WindowBuffer3x3Generics())

case class AcceleratorV1YCmd(generics: AcceleratorV1Generics) extends Bundle{
  val address = UInt(log2Up(generics.yMemDepth*4) bits)
  val data = Bits(32 bits)
}

case class AcceleratorV1Ctlr(generics: AcceleratorV1Generics) extends Component {
  val io = new Bundle {
    val init = in Bool
    val rowBufferRowWidth = in UInt (log2Up(generics.rowBufferConfig.maxRowWidth + 1) bit)
    val rowBufferInitialDelay = in UInt (log2Up((generics.rowBufferConfig.maxDelayValue()) + 1) bit)
    val outChannelCount = in UInt(log2Up(generics.yMemDepth + 1) bits)

    val x = slave Stream(Bits(32 bits))
    val y = slave Flow(AcceleratorV1YCmd(generics))
    val z = master Stream(Bits(32 bits))
  }

  val yMem0 = Mem(Bits(32 bits), generics.yMemDepth)
  val yMem1 = Mem(Bits(32 bits), generics.yMemDepth)
  val yMem2 = Mem(Bits(32 bits), generics.yMemDepth)
  val yMem3 = Mem(Bits(32 bits), generics.yMemDepth)

  val yMemAddr = Reg(yMem0.addressType) init(0)
  val yReadData = yMem3(yMemAddr) ## yMem2(yMemAddr) ## yMem1(yMemAddr) ## yMem0(yMemAddr)

  yMem0.write(
    enable = io.y.valid && (io.y.address(1 downto 0) === 0),
    address = (io.y.address >> 2).resized,
    data = io.y.data
  )
  yMem1.write(
    enable = io.y.valid && (io.y.address(1 downto 0) === 1),
    address = (io.y.address >> 2).resized,
    data = io.y.data
  )
  yMem2.write(
    enable = io.y.valid && (io.y.address(1 downto 0) === 2),
    address = (io.y.address >> 2).resized,
    data = io.y.data
  )
  yMem3.write(
    enable = io.y.valid && (io.y.address(1 downto 0) === 3),
    address = (io.y.address >> 2).resized,
    data = io.y.data
  )


  val ser32to8 = Ser32to8()
  ser32to8.io.input << io.x

  val windowBuffer3x3 = WindowBuffer3x3(generics.rowBufferConfig)
  windowBuffer3x3.io.init := False
  windowBuffer3x3.io.input << ser32to8.io.output
  windowBuffer3x3.io.rowWidth := io.rowBufferRowWidth
  windowBuffer3x3.io.initialDelay := io.rowBufferInitialDelay
  windowBuffer3x3.io.output.ready := False

  val mac8x9 = Mac8x9(Mac8x9Generics(useHwMultiplier = true))
  val mac8x9PayloadReg = Reg(mac8x9.io.cmd.x.asBits) init(0)
  mac8x9.io.cmd.valid := False
  mac8x9.io.cmd.payload.acc0 := 0
  mac8x9.io.cmd.payload.x.assignFromBits(mac8x9PayloadReg)
  mac8x9.io.cmd.payload.y.assignFromBits(yReadData(71 downto 0))

  io.z << mac8x9.io.rsp.translateWith(mac8x9.io.rsp.acc.asBits)

  val fsm = new StateMachine {
    always {
      when(io.init) {
        goto(sInit)
      }
    }

    val sInit = new State with EntryPoint
    val sGetNext = new State
    val sLoop = new State

    val yMemLastAddr = UInt(yMemAddr.getBitsWidth bits)
    yMemLastAddr := (io.outChannelCount - 1).resized
    val lastIteration = (yMemAddr === yMemLastAddr)

    sInit
      .whenIsActive {
        windowBuffer3x3.io.init := True
        goto(sGetNext)
      }
    sGetNext
      .whenIsActive {
        windowBuffer3x3.io.output.ready := True
        when(windowBuffer3x3.io.output.fire) {
          mac8x9PayloadReg := windowBuffer3x3.io.output.payload.asBits
          goto(sLoop)
        }
      }
    sLoop
      .onEntry {
        yMemAddr := 0
      }
      .whenIsActive {
        mac8x9.io.cmd.valid := True
        when(mac8x9.io.cmd.fire) {
          yMemAddr := yMemAddr + 1
          when (lastIteration) {
            goto(sGetNext)
          }
        }
      }
  }

  def driveFrom(bus : BusSlaveFactory, baseAddress : Int = 0) = new Area {
    require(generics.xFifoDepth >= 1)
    require(generics.zFifoDepth >= 1)
    require(bus.busDataWidth == 32)

    val busWithOffset = new BusSlaveFactoryAddressWrapper(bus, baseAddress)

    val XLogic = new Area {
      val streamUnbuffered = busWithOffset.createAndDriveFlow(Bits(32 bits),address =  0x100).toStream //x address
      val fifo = new StreamFifo(streamUnbuffered.payloadType, generics.xFifoDepth)

      fifo.io.flush := io.init
      fifo.io.push << streamUnbuffered
      io.x << fifo.io.pop
    }

    val YLogic = new Area {
      val mapping = SizeMapping(0x200, (generics.yMemDepth * 4) << log2Up(busWithOffset.busDataWidth/8)) //yMemDepth * 4 memory banks * bytes per word
      val memAddress = busWithOffset.writeAddress(mapping) >> log2Up(busWithOffset.busDataWidth/8)

      io.y.valid := False
      io.y.address := memAddress
      busWithOffset.nonStopWrite(io.y.data)
      busWithOffset.onWritePrimitive(mapping, true, null) {
        io.y.valid := True
      }
    }

    val ZLogic = new Area {
      val fifo = new StreamFifo(io.z.payloadType, generics.zFifoDepth)

      fifo.io.flush := io.init
      fifo.io.push << io.z

      busWithOffset.read(fifo.io.pop.payload, address = 0x104) //z address
      fifo.io.pop.ready := busWithOffset.isReading(0x104)
    }

    val statusLogic = new Area {
      io.init := False

      busWithOffset.read(XLogic.fifo.io.availability, address = 0x00, 0)
      busWithOffset.read(ZLogic.fifo.io.occupancy, address = 0x00, 16)
      busWithOffset.write(io.init, address = 0x00, 31)
    }

    val configLogic = new Area {
      busWithOffset.driveAndRead(io.rowBufferRowWidth, address = 0x04, 0) init(0)
      busWithOffset.driveAndRead(io.rowBufferInitialDelay, address = 0x04, 16) init(0)
      busWithOffset.driveAndRead(io.outChannelCount, address = 0x08, 0) init(1)
    }
  }
}

case class PipelinedMemoryAcceleratorV1Ctlr(config: AcceleratorV1Generics) extends Component{
  val io = new Bundle{
    val bus =  slave(PipelinedMemoryBus(12,32))
  }

  val accelCtlr = AcceleratorV1Ctlr(config)
  val busCtrl = new PipelinedMemoryBusSlaveFactory(io.bus)
  val bridge = accelCtlr.driveFrom(busCtrl)
}

object AcceleratorV1{
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog(AcceleratorV1Ctlr(AcceleratorV1Generics()))
  }
}

object AcceleratorV1DrivenFromBus{
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog({
      new Component{
        val accelCtlr = AcceleratorV1Ctlr(AcceleratorV1Generics(rowBufferConfig = WindowBuffer3x3Generics()))
        val busCtrl = new PipelinedMemoryBusSlaveFactory(slave(PipelinedMemoryBus(12,32)))
        accelCtlr.driveFrom(busCtrl, 0)
      }.setDefinitionName("AcceleratorV1DrivenFromBus")
    })
  }
}