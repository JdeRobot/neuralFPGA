package neuralfpga.umlp

import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusSlaveFactory}


//Peripheral component which create a timer, 3 writable leds, 1 UART transmitter
case class Peripherals(smallTimer : Boolean = false) extends Component{
  val io = new Bundle{
    val bus = slave(PipelinedMemoryBus(6, 32))
    val mTimeInterrupt = out Bool()
    val leds = out Bits(3 bits)
    //val serialTx = out Bool()
  }

  val mapper = new PipelinedMemoryBusSlaveFactory(io.bus)

  //Led logic
  mapper.driveAndRead(io.leds, 0x00, 0) init(0)

  //Uart transmiter logic
//  val serialTx = new Area{
//    val counter = Counter(12)
//    val buffer = Reg(Bits(8 bits))
//    val bitstream = buffer ## "0111"
//    val busy = counter =/= 0
//
//    io.serialTx := RegNext(bitstream(counter)) init(True)
//    val timer = CounterFreeRun(ClockDomain.current.frequency.getValue.toInt/serialBaudRate)
//    when(counter =/= 0 && timer.willOverflowIfInc){
//      counter.increment()
//    }
//
//    mapper.write(buffer, 0x0, 0)
//    when(mapper.isWriting(0x0)) { counter.increment() }
//    mapper.read(busy, 0x0, 0)
//  }

  //Timer logic
  val timer = if(!smallTimer) new Area {
    val counter = Reg(UInt(32 bits)) init(0)
    val cmp = Reg(UInt(32 bits)) init(0)
    val interrupt = RegInit(False) setWhen(!(counter - cmp).msb) clearWhen(mapper.isWriting(0x18))
    counter := counter + 1
    io.mTimeInterrupt := interrupt
    mapper.read(counter, 0x10)
    mapper.write(cmp, 0x18)
  } else new Area {
    val width = Math.max(20, log2Up(ClockDomain.current.frequency.getValue.toInt/100)) //downto to 100 hz
    val counter, cmp = Reg(UInt(width bits))
    val hit = counter === cmp
    val interrupt = RegInit(False) setWhen(hit) clearWhen(mapper.isWriting(0x10))
    counter := counter + 1
    when(hit || mapper.isWriting(0x18)){
      counter := 0
    }
    io.mTimeInterrupt := interrupt
    mapper.read(counter, 0x10)
    mapper.write(cmp, 0x18)
  }
}