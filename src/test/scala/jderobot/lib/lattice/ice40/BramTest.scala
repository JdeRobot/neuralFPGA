package jderobot.lib.lattice.ice40

import org.scalatest.FunSuite
import spinal.core._
import spinal.core.sim._
import spinal.lib.bus.simple.PipelinedMemoryBus

class BramTest extends FunSuite {
  class BramDut extends Component {
    val io = new Bundle {
      val load = in Bool()
      val ready = out Bool()
      val data = out Bits(32 bits)
    }

    val ram = Bram(64)
    val ramCtlr = new Area {
      val bus = PipelinedMemoryBus(ram.io.bus.config)
      val counter = Reg(UInt(bus.config.addressWidth bits)) init(0)

      bus.cmd.valid := io.load
      bus.cmd.address := counter
      bus.cmd.write := False
      bus.cmd.data.assignDontCare()
      bus.cmd.mask.assignDontCare()

      when (bus.cmd.fire) {
        counter := counter + 1
      }
    }

    ram.io.bus << ramCtlr.bus
    io.ready := ramCtlr.bus.rsp.valid
    io.data := ramCtlr.bus.rsp.data
  }

  val simConfig = SimConfig.withWave

  test("loadFromMemory") {
    simConfig.workspaceName("Bram").doSim(new BramDut) { dut =>
      dut.clockDomain.forkStimulus(period = 10)
      dut.io.load #= false

      dut.clockDomain.waitSampling(10)

      //test random values
      var idx = 0
      while(idx < 100){
        dut.io.load #= true
        waitUntil(dut.io.ready.toBoolean)
        dut.io.load #= false
        println(dut.io.data.toLong)
        waitUntil(!dut.io.ready.toBoolean)
        idx += 1
      }
    }
  }

}
