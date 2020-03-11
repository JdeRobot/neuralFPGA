package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._

class Mac8x9Test extends FunSuite {
  val simConfig = SimConfig.withWave

  test("mac ones") {
    simConfig.doSim(Mac8x9(Mac8x9Generics())){ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= 1)
      dut.io.cmd.y.foreach(_ #= 1)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true
      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == 9)
    }
  }

  test("mac ones HW multiplier") {
    simConfig.doSim(Mac8x9(Mac8x9Generics(useHwMultiplier = true))){ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= 1)
      dut.io.cmd.y.foreach(_ #= 1)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true
      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == 9)
    }
  }

  test("stall") {
    simConfig.doSim(Mac8x9(Mac8x9Generics())) { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.acc0 #= 0
      dut.io.cmd.y.foreach(_ #= 1)
      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      fork {
        dut.io.cmd.valid #= true

        var i = 0
        while (i < 10) {
          dut.io.cmd.x.foreach(_ #= i)
          dut.clockDomain.waitRisingEdgeWhere(dut.io.cmd.ready.toBoolean)
          i += 1
        }

        dut.io.cmd.valid #= false
        dut.io.cmd.x.foreach(_ #= 0)
      }

      fork {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
        while (dut.io.cmd.valid.toBoolean) {
          dut.io.rsp.ready #= !dut.io.cmd.ready.toBoolean
          dut.clockDomain.waitSampling()
        }

        dut.io.rsp.ready #= true
      }

      var j = 0;
      while (j < 10) {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.ready.toBoolean && dut.io.rsp.valid.toBoolean)
        assert(dut.io.rsp.acc.toLong == j * 9)
        j += 1
      }
    }
  }
}
