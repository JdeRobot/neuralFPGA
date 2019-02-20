package jderobot.lib.math

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import sys.process._

class UtilsTest extends FunSuite {
  class RoundingDivideByPOTDut extends Component {
    val io = new Bundle {
      val x = in SInt (32 bits)
      val exp = in UInt(5 bits)
      val result = out SInt (32 bits)
    }
    io.result := RegNext(RoundingDivideByPOT(io.x, io.exp)) init(0)
  }

  class SaturatingRoundingDoublingHighMulDut extends Component {
    val io = new Bundle {
      val a = in SInt (32 bits)
      val b = in SInt (32 bits)
      val result = out SInt (32 bits)
    }
    io.result := RegNext(SaturatingRoundingDoublingHighMul(io.a, io.b)) init(0)
  }

  val simConfig = SimConfig.withWave

  test("RoundingDivideByPOT") {
    simConfig.workspaceName("RoundingDivideByPOT").doSim(new RoundingDivideByPOTDut) { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      case class ReferenceSample(rawSample: Array[String]) {
        val x: Int = rawSample(0).toInt
        val exp: Int = rawSample(1).toInt
        val result: Int = rawSample(2).toInt
      }

      val reference =
        Seq("src/test/c/reference/rounding_divide_by_pot_32bit_reference", "1000").!!.split('\n').map(_.split(",")).map(ReferenceSample)
      var previousX = reference(0).x
      var previousExp = reference(0).exp
      var expectedResult = 0
      for(i <- reference){
        println(s"Reference x=${i.x}, exp=${i.exp}, result=${i.result}")
        dut.io.x #= i.x
        dut.io.exp #= i.exp
        dut.clockDomain.waitSampling()
        assert(
          dut.io.result.toInt == expectedResult,
          s"x=$previousX, exp=$previousExp.Expected=$expectedResult, got=${dut.io.result.toInt}"
        )
        expectedResult = i.result
        previousX = i.x
        previousExp = i.exp
      }
    }
  }

  test("SaturatingRoundingDoublingHighMulDut") {
    simConfig.workspaceName("SaturatingRoundingDoublingHighMulDut").doSim(new SaturatingRoundingDoublingHighMulDut) { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      case class ReferenceSample(rawSample: Array[String]) {
        val a: Int = rawSample(0).toInt
        val b: Int = rawSample(1).toInt
        val result: Int = rawSample(2).toInt
      }

      val reference =
        Seq("src/test/c/reference/saturating_rounding_doubling_high_mul_32bit_reference", "1000").!!.split('\n').map(_.split(",")).map(ReferenceSample)

      var previousA = reference(0).a
      var previousB = reference(0).b
      var expectedResult = 0
      for(i <- reference){
        //println(s"Reference a=${i.a}, b=${i.b}, result=${i.result}")
        dut.io.a #= i.a
        dut.io.b #= i.b
        dut.clockDomain.waitSampling()
        assert(
          dut.io.result.toInt == expectedResult,
          s"a=$previousA, b=$previousB.Expected=$expectedResult, got=${dut.io.result.toInt}"
        )
        expectedResult = i.result
        previousA = i.a
        previousB = i.b
      }
    }
  }
}
