package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._
import spinal.lib.BIG

class Ser32to8Test extends FunSuite {
  val simConfig = SimConfig.withWave

  test("ser32to8 little endian") {
    simConfig.doSim(Ser32to8()){ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= false

      dut.clockDomain.waitSampling()

      dut.io.input.valid #= true
      dut.io.input.payload #= 0x03020100
      dut.io.output.ready #= true

      var counter = 0
      while (!dut.io.input.ready.toBoolean) {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
        assert(dut.io.output.payload.toInt == counter)
        counter += 1
      }
    }
  }

  test("ser32to8 big endian") {
    simConfig.doSim(Ser32to8(endianness = BIG)){ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= false

      dut.clockDomain.waitSampling()

      dut.io.input.valid #= true
      dut.io.input.payload #= 0x00010203
      dut.io.output.ready #= true

      var counter = 0
      while (!dut.io.input.ready.toBoolean) {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
        assert(dut.io.output.payload.toInt == counter)
        counter += 1
      }
    }
  }

  test("ser32to8 resets counter when input not valid") {
    simConfig.doSim(Ser32to8()){ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= false

      dut.clockDomain.waitSampling()

      dut.io.input.valid #= true
      dut.io.input.payload #= 0x03020100
      dut.io.output.ready #= true

      dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
      assert(dut.io.output.payload.toInt == 0)

      //input not valid anymore, stop serialization
      dut.io.input.valid #= false
      dut.clockDomain.waitRisingEdgeWhere(!dut.io.output.valid.toBoolean)

      //new transaction
      dut.io.input.valid #= true
      dut.io.input.payload #= 0x03020100

      var counter = 0
      while (!dut.io.input.ready.toBoolean) {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
        assert(dut.io.output.payload.toInt == counter)
        counter += 1
      }
    }
  }
}
