package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._
import spinal.lib.BIG

class AdderTreex9Test extends FunSuite {
  val compiled = SimConfig.withWave.allOptimisation.compile{
    val dut = AdderTreex9(AdderTreex9Generics(inputWidth = 8, accumulatorWidth = 32))
    dut
  }

  test("adder tree add 9 times 1") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= 1)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true

      dut.io.cmd.x.foreach(_ #= 0)

      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == 9)
    }
  }

  test("adder tree add 9 times -1") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= -1)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true

      dut.io.cmd.x.foreach(_ #= 0)

      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == -9)
    }
  }

  test("adder tree add 9 times 127 (upper limit)") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= 127)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true

      dut.io.cmd.x.foreach(_ #= 0)

      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == 127 * 9)
    }
  }

  test("adder tree add 9 times -128 (lower limit)") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.valid #= false
      dut.io.rsp.ready #= false
      dut.clockDomain.waitSampling()

      dut.io.cmd.valid #= true
      dut.io.cmd.x.foreach(_ #= -128)
      dut.io.cmd.acc0 #= 0

      dut.clockDomain.waitSampling()
      dut.io.rsp.ready #= true

      dut.io.cmd.x.foreach(_ #= 0)

      dut.io.cmd.valid #= false

      dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.valid.toBoolean)
      assert(dut.io.rsp.acc.toLong == -128 * 9)
    }
  }

  test("stall") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.cmd.acc0 #= 0
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
      while(j < 10) {
        dut.clockDomain.waitRisingEdgeWhere(dut.io.rsp.ready.toBoolean && dut.io.rsp.valid.toBoolean)
        assert(dut.io.rsp.acc.toLong ==  j *  9)
        j += 1
      }
    }
  }
}
