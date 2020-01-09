package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._

import scala.collection.mutable
import scala.util.Random

class RowBuffer3x3Test extends FunSuite {
  val compiled = SimConfig.withWave.allOptimisation.compile{
    val dut = RowBuffer3x3(RowBuffer3x3Config(elementWidth = 8, maxRowWidth = 32))
    dut.rowShifter.writeOffset.simPublic()
    dut.rowShifter.readPtr.value.simPublic()
    dut.rowShifter.writePtr.simPublic()
    dut
  }

  test("flush") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.flush.valid #= false
      dut.io.flush.rowWidth #= 8
      dut.io.flush.initialDelay #= 16

      dut.clockDomain.waitSampling()

      dut.io.flush.valid #= true
      dut.clockDomain.waitSampling()
      dut.io.flush.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)
    }
  }

  test("load") {
    compiled.doSim{ dut =>
      val expectedWindows = mutable.Queue[Vector[Int]]()

      expectedWindows.enqueue(Vector(0,1,2, 8,9,10, 16,17,18))
      expectedWindows.enqueue(Vector(1,2,3, 9,10,11, 17,18,19))
      expectedWindows.enqueue(Vector(2,3,4, 10,11,12, 18,19,20))

      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.flush.valid #= false
      dut.io.flush.rowWidth #= 8
      dut.io.flush.initialDelay #= 16+3

      dut.clockDomain.waitSampling()

      dut.io.flush.valid #= true
      dut.clockDomain.waitSampling()
      dut.io.flush.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)

      val popThread = fork {
        while (!expectedWindows.isEmpty) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
          assert(dut.io.output.payload.map(f => f.toInt) == expectedWindows.dequeue())
        }
      }

      dut.io.input.valid #= true
      for(idx <- 0 until 32){
        dut.io.input.payload #= idx
        dut.clockDomain.waitSampling()
      }
      dut.io.input.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)

      popThread.join()
    }
  }

  test("loadAndFlush") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.flush.valid #= false
      dut.io.flush.rowWidth #= 8
      dut.io.flush.initialDelay #= 16+3

      dut.clockDomain.waitSampling()

      dut.io.flush.valid #= true
      dut.clockDomain.waitSampling()
      dut.io.flush.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)


      dut.io.input.valid #= true
      for(idx <- 0 until 24){
        dut.io.input.payload #= idx
        dut.clockDomain.waitSampling()
      }
      dut.io.input.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 24)
      assert(dut.rowShifter.writePtr.toInt == 0)

      dut.io.flush.valid #= true
      dut.clockDomain.waitSampling()
      dut.io.flush.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)
    }
  }
}
