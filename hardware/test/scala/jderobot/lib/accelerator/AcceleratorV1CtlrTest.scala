package jderobot.lib.accelerator

import org.scalatest.FunSuite
import spinal.core.sim._

import scala.collection.mutable

class AcceleratorV1CtlrTest extends FunSuite {
  val compiled = SimConfig.withWave.allOptimisation.compile{
    val dut = AcceleratorV1Ctlr(AcceleratorV1Generics())
    dut
  }

  test("compute_1_channel") {
    compiled.doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.init #= false
      dut.io.rowBufferRowWidth #= 4
      dut.io.rowBufferInitialDelay #= 4 + 4 + 2 //2 rows + 3 pixels
      dut.io.outChannelCount #= 1

      dut.io.x.valid #= false
      dut.io.y.valid #= false
      dut.io.z.ready #= false//dut.io.z.valid.toBoolean //consume transactions immediately

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      //with rowWith=4 and 3x3 windows we are expecting
      // 4 (2 are invalid) + 2 results
      val popThread = fork {
        for (idx <- 0 until 6) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.z.valid.toBoolean && dut.io.z.ready.toBoolean)
        }
      }

      fork {
        //wait till data ready detected, then consume one
        while(true) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.z.valid.toBoolean && !dut.io.z.ready.toBoolean)
          dut.io.z.ready #= true
          dut.clockDomain.waitSampling()
          dut.io.z.ready #= false
        }
      }

      dut.io.x.valid #= true
      for (idx <- 0 until 4) { //each x 32 bits value is 4 pixels
        dut.io.x.payload #= 0x04030201
        dut.clockDomain.waitRisingEdgeWhere(dut.io.x.ready.toBoolean)
      }
      dut.io.x.valid #= false
      dut.clockDomain.waitSampling()

      popThread.join()
    }
  }

  test("compute_8_channel") {
    compiled.doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.init #= false
      dut.io.rowBufferRowWidth #= 4
      dut.io.rowBufferInitialDelay #= 4 + 4 + 2 //2 rows + 3 pixels
      dut.io.outChannelCount #= 8

      dut.io.x.valid #= false
      dut.io.y.valid #= false
      dut.io.z.ready #= false

      dut.clockDomain.waitSampling()

      dut.io.init #= true
      dut.clockDomain.waitSampling()
      dut.io.init #= false
      dut.clockDomain.waitSampling()

      //with rowWith=4 and 3x3 windows we are expecting
      // (4 (2 are invalid) + 2 results) * 8 channels
      val popThread = fork {
        for (idx <- 0 until 6*8) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.z.valid.toBoolean && dut.io.z.ready.toBoolean)
        }
      }

      fork {
        //wait till data ready detected, then consume one
        while(true) {
          dut.clockDomain.waitRisingEdgeWhere(dut.io.z.valid.toBoolean && !dut.io.z.ready.toBoolean)
          dut.io.z.ready #= true
          dut.clockDomain.waitSampling()
          dut.io.z.ready #= false
        }
      }

      dut.io.x.valid #= true
      for (idx <- 0 until 4) { //each x 32 bits value is 4 pixels
        dut.io.x.payload #= 0x04030201
        dut.clockDomain.waitRisingEdgeWhere(dut.io.x.ready.toBoolean)
      }
      dut.io.x.valid #= false
      dut.clockDomain.waitSampling()

      popThread.join()
    }
  }
}
