package jderobot.lib.lattice.ice40

import org.scalatest.FunSuite
import spinal.core.ClockDomain
import spinal.core.sim._

class Multiplier8x8x2Test extends FunSuite {
  val simConfig = SimConfig.withWave

  test("signed multiplication") {
    simConfig.doSim(Multiplier8x8x2Signed()) { dut =>
      dut.io.a(0) #= 2
      dut.io.b(0) #= -2
      dut.io.a(1) #= 4
      dut.io.b(1) #= -4

      sleep(1)

      assert(dut.io.output(0).toInt == -4)
      assert(dut.io.output(1).toInt == -16)
    }
  }

  test("unsigned multiplication") {
    simConfig.doSim(Multiplier8x8x2Unsigned()) { dut =>
      dut.io.a(0) #= 2
      dut.io.b(0) #= 2
      dut.io.a(1) #= 4
      dut.io.b(1) #= 4

      sleep(1)

      assert(dut.io.output(0).toInt == 4)
      assert(dut.io.output(1).toInt == 16)
    }
  }

}
