package jderobot.lib.accelerator

import spinal.core._
import spinal.lib._

case class Ser32to8(endianness: Endianness = LITTLE) extends Component {
  val io = new Bundle {
    val input = slave Stream(Bits(32 bits))
    val output = master Stream(Bits(8 bits))
  }

  val counter = Counter(stateCount = 4, inc = io.output.fire)
  when(!io.input.valid) {
    counter.clear()
  }

  io.output.valid := io.input.valid
  endianness match {
    case `LITTLE` => io.output.payload.assignFromBits(io.input.payload.subdivideIn(4 slices).read(counter))
    case `BIG`    => io.output.payload.assignFromBits(io.input.payload.subdivideIn(4 slices).reverse.read(counter))
  }
  io.input.ready := io.output.ready && counter.willOverflowIfInc
}
