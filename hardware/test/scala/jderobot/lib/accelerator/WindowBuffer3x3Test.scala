package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._

import scala.collection.mutable

class WindowBuffer3x3Test extends FunSuite {
  val compiled = SimConfig.withWave.allOptimisation.compile{
    val dut = WindowBuffer3x3(WindowBuffer3x3Generics(elementWidth = 8, maxRowWidth = 32))
    dut.rowShifter.writeOffset.simPublic()
    dut.rowShifter.readPtr.value.simPublic()
    dut.rowShifter.writePtr.simPublic()
    dut
  }

  test("flush") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= false
      dut.io.init #= false
      dut.io.rowWidth #= 8
      dut.io.initialDelay #= 16

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)
    }
  }

  test("load") {
    compiled.doSim{ dut =>
      val expectedWindows = mutable.Queue[Vector[Int]]()

      expectedWindows.enqueue(Vector(1,2,3, 1,2,3, 1,2,3))
      expectedWindows.enqueue(Vector(2,3,4, 2,3,4, 2,3,4))
      expectedWindows.enqueue(Vector(3,4,1, 3,4,1, 3,4,1))
      expectedWindows.enqueue(Vector(4,1,2, 4,1,2, 4,1,2))
      expectedWindows.enqueue(Vector(1,2,3, 1,2,3, 1,2,3))
      expectedWindows.enqueue(Vector(2,3,4, 2,3,4, 2,3,4))

      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= true
      dut.io.init #= false
      dut.io.rowWidth #= 4
      dut.io.initialDelay #= 4+4+2 //2 rows + 2 pixels

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 4)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 4)

      val popThread = fork {
        while (!expectedWindows.isEmpty) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean)
          assert(dut.io.output.payload.map(f => f.toInt) == expectedWindows.dequeue())
        }
      }

      dut.io.input.valid #= true
      for(idx <- 0 until 16){
        dut.io.input.payload #= (idx % 4) + 1
        dut.clockDomain.waitRisingEdgeWhere(dut.io.input.ready.toBoolean)
      }

      dut.io.input.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 4)
      assert(dut.rowShifter.readPtr.value.toInt == 16)
      assert(dut.rowShifter.writePtr.toInt == 20)

      dut.clockDomain.waitRisingEdgeWhere(!dut.io.output.valid.toBoolean)

      popThread.join()
    }
  }

  test("stall") {
    compiled.doSim{ dut =>
      val expectedWindows = mutable.Queue[Vector[Int]]()

      expectedWindows.enqueue(Vector(1,2,3, 1,2,3, 1,2,3))
      expectedWindows.enqueue(Vector(2,3,4, 2,3,4, 2,3,4))
      expectedWindows.enqueue(Vector(3,4,1, 3,4,1, 3,4,1))
      expectedWindows.enqueue(Vector(4,1,2, 4,1,2, 4,1,2))
      expectedWindows.enqueue(Vector(1,2,3, 1,2,3, 1,2,3))
      expectedWindows.enqueue(Vector(2,3,4, 2,3,4, 2,3,4))

      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= false
      dut.io.init #= false
      dut.io.rowWidth #= 4
      dut.io.initialDelay #= 4+4+2 //2 rows + 3 pixels

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 4)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 4)

      val popThread = fork {
        while (!expectedWindows.isEmpty) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.output.valid.toBoolean && dut.io.output.ready.toBoolean)
          assert(dut.io.output.payload.map(f => f.toInt) == expectedWindows.dequeue())
        }
      }

      fork {
        dut.clockDomain.waitRisingEdgeWhere(!dut.io.input.ready.toBoolean)
        while (dut.io.input.valid.toBoolean) {
          dut.io.output.ready #= !dut.io.input.ready.toBoolean
          dut.clockDomain.waitSampling(5)
        }

        dut.io.output.ready #= true
      }

      dut.io.input.valid #= true
      for(idx <- 0 until 16){
        dut.io.input.payload #= (idx % 4) + 1
        dut.clockDomain.waitRisingEdgeWhere(dut.io.input.ready.toBoolean)
      }

      dut.io.input.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 4)
      assert(dut.rowShifter.readPtr.value.toInt == 16)
      assert(dut.rowShifter.writePtr.toInt == 20)

      dut.clockDomain.waitRisingEdgeWhere(!dut.io.output.valid.toBoolean)

      popThread.join()
    }
  }

  test("loadAndFlush") {
    compiled.doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input.valid #= false
      dut.io.output.ready #= true
      dut.io.init #= false
      dut.io.rowWidth #= 8
      dut.io.initialDelay #= 16+3

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)


      dut.io.input.valid #= true
      for(idx <- 0 until 24){
        dut.io.input.payload #= idx
        dut.clockDomain.waitRisingEdgeWhere(dut.io.input.ready.toBoolean)
      }

      dut.io.input.valid #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 24)
      assert(dut.rowShifter.writePtr.toInt == 0)

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      assert(dut.rowShifter.writeOffset.toInt == 8)
      assert(dut.rowShifter.readPtr.value.toInt == 0)
      assert(dut.rowShifter.writePtr.toInt == 8)
    }
  }
}
