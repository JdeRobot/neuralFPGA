package jderobot.lib.math

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import sys.process._

import scala.util.Random

class UtilsTest extends FunSuite {
  class RoundingDivideByPOTDut extends Component {
    val io = new Bundle {
      val operand = in SInt (32 bits)
      val x = in UInt(5 bits)
      val result = out SInt (32 bits)
    }
    io.result := RegNext(RoundingDivideByPOT(io.operand, io.x)) init(0)
  }

  class SaturatingRoundingDoublingHighMulDut extends Component {
    val io = new Bundle {
      val a = in SInt (8 bits)
      val b = in SInt (8 bits)
      val result = out SInt (8 bits)
    }
    io.result := RegNext(SaturatingRoundingDoublingHighMul(io.a, io.b)) init(0)
  }

  val simConfig = SimConfig.withWave

  test("RoundingDivideByPOT") {
    simConfig.doSim(new RoundingDivideByPOTDut) { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      val reference = "src/test/c/reference/rounding_divide_by_pot_32bit_reference".!!.split('\n').map(_.split(",").map(_.trim.toInt))
      var previousOperand = reference(0)(0)
      var previousX = reference(0)(1)
      var expectedResult = 0
      for(i <- reference){
        println(s"Reference operand=${i(0)}, x=${i(1)}, result=${i(2)}")
        dut.io.operand #= i(0)
        dut.io.x #= i(1)
        dut.clockDomain.waitSampling()
        assert(
          dut.io.result.toInt == expectedResult,
          s"operand=${previousOperand}, x=${previousX}.Expected=${expectedResult}, got=${dut.io.result.toInt}"
        )
        expectedResult = i(2)//dut.io.operand.toInt / Math.pow(2, dut.io.x.toInt).toInt
        previousOperand = i(0)
        previousX = i(1)
      }
    }
  }

  test("SaturatingRoundingDoublingHighMulDut") {
    simConfig.doSim(new SaturatingRoundingDoublingHighMulDut) { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      var expectedResult = 0
      for(idx <- 0 until 1000){
        dut.io.a #= Random.nextInt(256) - 128 //map to -128..127
        dut.io.b #= Random.nextInt(256) - 128 //map to -128..127
        dut.clockDomain.waitSampling()
        //        assert(
        //          dut.io.result.toInt == expectedResult,
        //          "operand=" + dut.io.operand.toInt + ", x=" + dut.io.x.toInt + ".Expected=" + expectedResult + ", got=" + dut.io.result.toInt
        //        )
        //        expectedResult = dut.io.operand.toInt / Math.pow(2, dut.io.x.toInt).toInt
      }
    }
  }
}
