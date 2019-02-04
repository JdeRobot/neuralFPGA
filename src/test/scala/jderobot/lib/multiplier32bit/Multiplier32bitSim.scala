package jderobot.lib.multiplier32bit

import spinal.core._
import spinal.core.sim._

import scala.util.Random

object Multiplier32bitSim {
  def main(args: Array[String]) {
    val simConfig = SimConfig.withWave

    simConfig.doSim(Multiplier32bit(Multiplier32bitImpl().oneCycleMultiplier)) { dut =>

      dut.clockDomain.forkStimulus(period = 10)

      dut.io.valid #= false

      dut.clockDomain.waitSampling(10)

      //test edges
      // min * min
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MinValue
      dut.io.multiplier #= Int.MinValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MinValue, Int.MinValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // min * max
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MinValue
      dut.io.multiplier #= Int.MaxValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MinValue, Int.MaxValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // max * min
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MaxValue
      dut.io.multiplier #= Int.MinValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MaxValue, Int.MinValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // max * max
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MaxValue
      dut.io.multiplier #= Int.MaxValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MaxValue, Int.MaxValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      //test random values
      var idx = 0
      while(idx < 100){
        val a, b = Random.nextInt()
        dut.io.valid #= true
        dut.io.multiplicand #= a
        dut.io.multiplier #= b
        waitUntil(dut.io.ready.toBoolean)
        assert(dut.io.result.toLong == (a.toLong * b.toLong))
        dut.io.valid #= false
        waitUntil(!dut.io.ready.toBoolean)
        idx += 1
      }
    }

    simConfig.doSim(Multiplier32bit(Multiplier32bitImpl().boothMultiplier)) { dut =>

      dut.clockDomain.forkStimulus(period = 10)

      dut.io.valid #= false

      dut.clockDomain.waitSampling(10)

      //test edges
      // min * min
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MinValue
      dut.io.multiplier #= Int.MinValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MinValue, Int.MinValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // min * max
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MinValue
      dut.io.multiplier #= Int.MaxValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MinValue, Int.MaxValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // max * min
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MaxValue
      dut.io.multiplier #= Int.MinValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MaxValue, Int.MinValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      // max * max
      dut.io.valid #= true
      dut.io.multiplicand #= Int.MaxValue
      dut.io.multiplier #= Int.MaxValue
      waitUntil(dut.io.ready.toBoolean)
      assertMultiplication(Int.MaxValue, Int.MaxValue, dut.io.result.toLong)
      dut.io.valid #= false
      waitUntil(!dut.io.ready.toBoolean)

      var idx = 0
      while(idx < 1000){
        val a, b = Random.nextInt()
        dut.io.valid #= true
        dut.io.multiplicand #= a
        dut.io.multiplier #= b
        waitUntil(dut.io.ready.toBoolean)
        assertMultiplication(a, b, dut.io.result.toLong)
        dut.io.valid #= false
        waitUntil(!dut.io.ready.toBoolean)
        idx += 1
      }
    }
  }

  def assertMultiplication(a: Int, b: Int, result: Long): Unit = {
    assert(result == (a.toLong * b.toLong),
      "Expected " + a + "*" + b + "=" + (a.toLong * b.toLong) + ", got=" + result)
  }
}
