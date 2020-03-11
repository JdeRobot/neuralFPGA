package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.{BusSlaveFactory, BusSlaveFactoryAddressWrapper}
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusSlaveFactory}

case class AcceleratorV1Generics(xFifoDepth: Int = 256,
                                 zFifoDepth: Int = 256,
                                 rowBufferConfig: WindowBuffer3x3Generics)

case class AcceleratorV1ConfigCmd(generics: AcceleratorV1Generics) extends Bundle {
  val rowBufferFlushCmd = WindowBuffer3x3FlushCmd(generics.rowBufferConfig)
}

case class AcceleratorV1Io(generics: AcceleratorV1Generics) extends Bundle {
  val config = slave(Flow(AcceleratorV1ConfigCmd(generics)))
  val x = slave(Stream(Bits(32 bits)))
  //val y = slave(Stream(Bits(128 bits)))
  //val z0 = slave(Stream(Bits(32 bits)))
  val z = master(Stream(Bits(32 bits)))

  def driveFrom(bus : BusSlaveFactory, baseAddress : Int = 0) = new Area {
    require(generics.xFifoDepth >= 1)
    require(generics.zFifoDepth >= 1)
    require(bus.busDataWidth == 32)

    val busWithOffset = new BusSlaveFactoryAddressWrapper(bus, baseAddress)

    val XLogic = new Area {
      val streamUnbuffered = busWithOffset.createAndDriveFlow(Bits(32 bits),address =  0x08).toStream //x address 4
      val (stream, fifoAvailability) = streamUnbuffered.queueWithAvailability(generics.xFifoDepth)
      x << stream
    }

//    val YLogic = new Area {
//      val streamUnbuffered = busWithOffset.createAndDriveFlow(Bits(128 bits),address =  0).toStream
//      val (stream, fifoAvailability) = streamUnbuffered.queueWithAvailability(config.fifoDepth)
//      y << stream
//      busWithOffset.read(fifoAvailability, address = 16, 16) //FIXME
//    }
//
//    val Z0Logic = new Area {
//      val streamUnbuffered = busWithOffset.createAndDriveFlow(Bits(128 bits),address =  0).toStream
//      val (stream, fifoAvailability) = streamUnbuffered.queueWithAvailability(config.fifoDepth)
//      z0 << stream
//      busWithOffset.read(fifoAvailability, address = 16, 16) //FIXME
//    }

    val ZLogic = new Area {
      val (stream, fifoOccupancy) = z.queueWithOccupancy(generics.zFifoDepth)
      val wordCount = (widthOf(stream.payload) - 1) / busWithOffset.busDataWidth + 1

      busWithOffset.readMultiWord(stream.payload, address = 0x0c) //z address
      stream.ready := busWithOffset.isReading(0x0c + ((wordCount - 1) * busWithOffset.wordAddressInc))
    }

    val statusLogic = new Area {
      busWithOffset.read(XLogic.fifoAvailability, address = 0x00, 0)
      busWithOffset.read(ZLogic.fifoOccupancy, address = 0x00, 16)
    }

    val configLogic = new Area {
      val valid = RegNext(busWithOffset.isWriting(address = 0x04)) init(False)

      busWithOffset.driveAndRead(config.rowBufferFlushCmd.rowWidth, address = 0x04, 0) init(0)
      busWithOffset.driveAndRead(config.rowBufferFlushCmd.initialDelay, address = 0x04, 16) init(0)

      config.valid := valid
    }
  }
}

case class AcceleratorV1Ctlr(generics: AcceleratorV1Generics) extends Component {
  val io = AcceleratorV1Io(generics)

  val ser32to8 = Ser32to8()
  val windowBuffer3x3 = WindowBuffer3x3(generics.rowBufferConfig)
  windowBuffer3x3.io.flush << io.config.translateWith(io.config.rowBufferFlushCmd)

  val mac8x9 = Mac8x9(Mac8x9Generics(useHwMultiplier = true))

  ser32to8.io.input << io.x
  windowBuffer3x3.io.input << ser32to8.io.output

  mac8x9.io.cmd.translateFrom(windowBuffer3x3.io.output)((to, from) => {
    to.acc0 := 0
    to.x := Vec(from.map(_.asUInt))
    to.y := Vec(U(1, 8 bits), 9)
  })

  io.z << mac8x9.io.rsp.translateWith(mac8x9.io.rsp.acc.asBits)
}

case class PipelinedMemoryAcceleratorV1Ctlr(config: AcceleratorV1Generics) extends Component{
  val io = new Bundle{
    val bus =  slave(PipelinedMemoryBus(12,32))
  }

  val accelCtlr = AcceleratorV1Ctlr(config)
  val busCtrl = new PipelinedMemoryBusSlaveFactory(io.bus)
  val bridge = accelCtlr.io.driveFrom(busCtrl)
}

object UseAcceleratorV1{
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir
    ).generateVerilog({
      new Component{
        val accelCtlr = AcceleratorV1Ctlr(AcceleratorV1Generics(rowBufferConfig = WindowBuffer3x3Generics(8, 256)))
        val busCtrl = new PipelinedMemoryBusSlaveFactory(slave(PipelinedMemoryBus(12,32)))
        accelCtlr.io.driveFrom(busCtrl, 0)
      }.setDefinitionName("UseAcceleratorV1")
    })
  }
}