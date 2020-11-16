package jderobot.lib.com.ft245

import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
import spinal.lib.io.{InOutWrapper, TriState}

case class Ft245Sync(useSIWU: Boolean = false) extends Bundle with IMasterSlave {
  val clkout = Bool
  val data = TriState(Bits(8 bits))
  val rxf = Bool
  val txe = Bool
  val rd = Bool
  val wr = Bool
  val oe = Bool
  val siwu = if (useSIWU) Bool else null

  override def asMaster(): Unit = {
    out(rd, wr, oe)
    if (useSIWU) out(siwu)
    in(clkout, rxf, txe)
    master(data)
  }
}

case class Ft245SyncNullReaderCtrl() extends Component {
  val io = new Bundle {
    val ft245 = master(Ft245Sync())
  }

  val coreClockDomain = ClockDomain(clock = io.ft245.clkout, frequency = FixedFrequency(60 MHz))

  val coreArea = new ClockingArea(coreClockDomain) {
    // ft245 signals are active low
    val rxf = !io.ft245.rxf

    val oe_reg = RegNext(rxf) init(False)
    val rd_reg = RegNext(rxf === oe_reg) init(False)

    io.ft245.oe := !oe_reg
    io.ft245.rd := !rd_reg

    io.ft245.wr := True
    io.ft245.data.write.assignDontCare()
    io.ft245.data.writeEnable := False
  }
}

case class Ft245SyncDummyWriterCtrl() extends Component {
  val io = new Bundle {
    val ft245 = master(Ft245Sync())
  }

  val coreClockDomain = ClockDomain(clock = io.ft245.clkout, frequency = FixedFrequency(60 MHz))

  val coreArea = new ClockingArea(coreClockDomain) {
    // ft245 signals are active low
    val txe = !io.ft245.txe

    val wr_reg = RegNext(txe) init (False)
    val data = Reg(UInt(8 bits)) init (0)


    when(wr_reg) {
      data := data + 1
    }

    io.ft245.wr := !wr_reg
    io.ft245.data.write := data.asBits
    io.ft245.data.writeEnable := True

    io.ft245.oe := True
    io.ft245.rd := True
  }
}

case class Ft245SyncNullReaderDummyWriterCtrl() extends Component {
  val io = new Bundle {
    val ft245 = master(Ft245Sync())
  }

  val coreClockDomain = ClockDomain(clock = io.ft245.clkout, frequency = FixedFrequency(60 MHz))

  val coreArea = new ClockingArea(coreClockDomain) {
    // ft245 signals are active low
    val rxf_reg = !io.ft245.rxf
    val txe_reg = !io.ft245.txe

    val oe_reg = Reg(Bool) init (False)
    val rd_reg = Reg(Bool) init (False)
    val wr_reg = Reg(Bool) init (False)

    val data = Reg(UInt(8 bits)) init (0)

    when(wr_reg) {
      data := data + 1
    }

    val fsm = new StateMachine {
      val sIdle = new State with EntryPoint
      val sReadOE = new State
      val sRead = new State
      val sWrite = new State

      sIdle
        .whenIsActive {
          when(rxf_reg) {
            oe_reg := True
            goto(sReadOE)
          }
          when(txe_reg) {
            wr_reg := True
            goto(sWrite)
          }
        }
      sReadOE
        .whenIsActive {
          rd_reg := True
          goto(sRead)
        }
      sRead
        .whenIsActive {
          when(!rxf_reg) {
            oe_reg := False
            rd_reg := False
            goto(sIdle)
          }
        }
      sWrite
        .whenIsActive {
          when(!txe_reg) {
            wr_reg := False
            goto(sIdle)
          }
        }
    }

    // ft245 signals are active low
    io.ft245.oe := !oe_reg
    io.ft245.rd := !rd_reg
    io.ft245.wr := !wr_reg

    io.ft245.data.write := data.asBits
    io.ft245.data.writeEnable := wr_reg
  }
}

object Ft245SyncNullReaderCtrl {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(InOutWrapper(Ft245SyncNullReaderCtrl()))
  }
}

object Ft245SyncDummyWriterCtrl {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(InOutWrapper(Ft245SyncDummyWriterCtrl()))
  }
}

object Ft245SyncNullReaderDummyWriterCtrl {
  def main(args: Array[String]) {
    val outRtlDir = if (!args.isEmpty) args(0) else  "rtl"
    SpinalConfig(
      targetDirectory = outRtlDir,
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = BOOT
      )
    ).generateVerilog(InOutWrapper(Ft245SyncNullReaderDummyWriterCtrl()))
  }
}