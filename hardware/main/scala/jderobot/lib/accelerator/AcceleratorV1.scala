package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.{BusSlaveFactory, BusSlaveFactoryAddressWrapper}
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusSlaveFactory}

case class AcceleratorV1Config(xFifoDepth: Int = 256, zFifoDepth: Int = 256)

case class AcceleratorV1Io(config: AcceleratorV1Config) extends Bundle {
  val x = slave(Stream(Bits(32 bits)))
  //val y = slave(Stream(Bits(128 bits)))
  //val z0 = slave(Stream(Bits(32 bits)))
  val z = master(Stream(Bits(32 bits)))

  def driveFrom(bus : BusSlaveFactory, baseAddress : Int = 0) = new Area {
    require(config.xFifoDepth >= 1)
    require(config.zFifoDepth >= 1)
    require(bus.busDataWidth == 32)

    val busWithOffset = new BusSlaveFactoryAddressWrapper(bus, baseAddress)

    val XLogic = new Area {
      val streamUnbuffered = busWithOffset.createAndDriveFlow(Bits(32 bits),address =  0x04).toStream //x address 4
      val (stream, fifoAvailability) = streamUnbuffered.queueWithAvailability(config.xFifoDepth)
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
      val (stream, fifoOccupancy) = z.queueWithOccupancy(config.zFifoDepth)
      val wordCount = (widthOf(stream.payload) - 1) / busWithOffset.busDataWidth + 1

      busWithOffset.readMultiWord(stream.payload, address = 0x08) //z address 4+4=8
      stream.ready := busWithOffset.isReading(0x08 + ((wordCount - 1) * busWithOffset.wordAddressInc))
    }

    val statusLogic = new Area {
      busWithOffset.read(XLogic.fifoAvailability, address = 0x00, 0)
      busWithOffset.read(ZLogic.fifoOccupancy, address = 0x00, 16)
    }
  }
}

case class AcceleratorV1Ctlr(config: AcceleratorV1Config) extends Component {
  val io = AcceleratorV1Io(config)

  val mac8x9 = Mac8x9(Mac8x9Config(useHwMultiplier = true))

  mac8x9.io.cmd.translateFrom(io.x)((to, from) => {
    to.acc0 := 0
    to.x := Vec(from.subdivideIn(8 bits).map(_.asUInt) ++ from.subdivideIn(8 bits).map(_.asUInt) :+ U(0, 8 bits))
    to.y := Vec(U(1, 8 bits), 9)
  })

  io.z.translateFrom(mac8x9.io.rsp)((to, from) => {
    to := from.acc.asBits
  })
}

case class PipelinedMemoryAcceleratorV1Ctlr(config: AcceleratorV1Config) extends Component{
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
        val accelCtlr = AcceleratorV1Ctlr(AcceleratorV1Config())
        val busCtrl = new PipelinedMemoryBusSlaveFactory(slave(PipelinedMemoryBus(12,32)))
        accelCtlr.io.driveFrom(busCtrl, 0)
      }.setDefinitionName("UseAcceleratorV1")
    })
  }
}