package neuralfpga.core

import vexriscv._
import spinal.core._
import spinal.lib._
import spinal.lib.bus.simple._
import vexriscv.plugin.{IntAluPlugin, Plugin}


case class DBusCmd() extends Bundle{
  val wr = Bool
  val address = UInt(32 bits)
  val data = Bits(32 bit)
  val size = UInt(2 bit)
}

case class DBusRsp() extends Bundle with IMasterSlave{
  val ready = Bool
  val error = Bool
  val data = Bits(32 bit)

  override def asMaster(): Unit = {
    out(ready,error,data)
  }
}


object DBusBus{
  def getPipelinedMemoryBusConfig() = PipelinedMemoryBusConfig(
    addressWidth = 32,
    dataWidth = 32
  )

}

case class DBusBus() extends Bundle with IMasterSlave{
  val cmd = Stream(DBusCmd())
  val rsp = DBusRsp()

  override def asMaster(): Unit = {
    master(cmd)
    slave(rsp)
  }

  def toPipelinedMemoryBus() : PipelinedMemoryBus = {
    val pipelinedMemoryBusConfig = DBusBus.getPipelinedMemoryBusConfig()
    val bus = PipelinedMemoryBus(pipelinedMemoryBusConfig)
    bus.cmd.valid := cmd.valid
    bus.cmd.write := cmd.wr
    bus.cmd.address := cmd.address.resized
    bus.cmd.data := cmd.data
    bus.cmd.mask := cmd.size.mux(
      0 -> B"0001",
      1 -> B"0011",
      default -> B"1111"
    ) |<< cmd.address(1 downto 0)
    cmd.ready := bus.cmd.ready

    rsp.ready := bus.rsp.valid
    rsp.data := bus.rsp.data

    bus
  }
}


class DBusPlugin(catchAddressMisaligned : Boolean = false,
                       catchAccessFault : Boolean = false,
                       earlyInjection : Boolean = false, /*, idempotentRegions : (UInt) => Bool = (x) => False*/
                       emitCmdInMemoryStage : Boolean = false,
                       onlyLoadWords : Boolean = false) extends Plugin[VexRiscv]{

  var dBus  : DBusBus = null
  assert(!(emitCmdInMemoryStage && earlyInjection))

  object MEMORY_ENABLE extends Stageable(Bool)
  object MEMORY_READ_DATA extends Stageable(Bits(32 bits))
  object MEMORY_ADDRESS_LOW extends Stageable(UInt(2 bits))
  object ALIGNEMENT_FAULT extends Stageable(Bool)

  var memoryExceptionPort : Flow[ExceptionCause] = null
  var rspStage : Stage = null

  override def setup(pipeline: VexRiscv): Unit = {
    import Riscv._
    import pipeline.config._
    import pipeline._

    val decoderService = pipeline.service(classOf[DecoderService])

    val stdActions = List[(Stageable[_ <: BaseType],Any)](
      SRC1_CTRL         -> Src1CtrlEnum.RS,
      SRC_USE_SUB_LESS  -> False,
      MEMORY_ENABLE     -> True,
      RS1_USE          -> True
    ) ++ (if(catchAccessFault || catchAddressMisaligned) List(IntAluPlugin.ALU_CTRL -> IntAluPlugin.AluCtrlEnum.ADD_SUB) else Nil) //Used for access fault bad address in memory stage

    val loadActions = stdActions ++ List(
      SRC2_CTRL -> Src2CtrlEnum.IMI,
      REGFILE_WRITE_VALID -> True,
      BYPASSABLE_EXECUTE_STAGE -> False,
      BYPASSABLE_MEMORY_STAGE  -> Bool(earlyInjection)
    ) ++ (if(catchAccessFault || catchAddressMisaligned) List(HAS_SIDE_EFFECT -> True) else Nil)

    val storeActions = stdActions ++ List(
      SRC2_CTRL -> Src2CtrlEnum.IMS,
      RS2_USE -> True
    )

    decoderService.addDefault(MEMORY_ENABLE, False)
    decoderService.add(
      (if(onlyLoadWords) List(LW) else List(LB, LH, LW, LBU, LHU, LWU)).map(_ -> loadActions) ++
      List(SB, SH, SW).map(_ -> storeActions)
    )



    rspStage = if(stages.last == execute) execute else (if(emitCmdInMemoryStage) writeBack else memory)
    if(catchAccessFault || catchAddressMisaligned) {
      val exceptionService = pipeline.service(classOf[ExceptionService])
      memoryExceptionPort = exceptionService.newExceptionPort(rspStage)
    }
  }

  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    dBus = master(DBusBus()).setName("dBus")


    //Emit dBus.cmd request
    val cmdStage = if(emitCmdInMemoryStage) memory else execute
    cmdStage plug new Area{
      import cmdStage._

      val cmdSent =  if(rspStage == execute) RegInit(False) setWhen(dBus.cmd.fire) clearWhen(!execute.arbitration.isStuck) else False

      insert(ALIGNEMENT_FAULT) := {
        if (catchAddressMisaligned)
          (dBus.cmd.size === 2 && dBus.cmd.address(1 downto 0) =/= 0) || (dBus.cmd.size === 1 && dBus.cmd.address(0 downto 0) =/= 0)
        else
          False
      }

      dBus.cmd.valid := arbitration.isValid && input(MEMORY_ENABLE) && !arbitration.isStuckByOthers && !arbitration.isFlushed && !input(ALIGNEMENT_FAULT) && !cmdSent
      dBus.cmd.wr := input(INSTRUCTION)(5)
      dBus.cmd.address := input(SRC_ADD).asUInt
      dBus.cmd.size := input(INSTRUCTION)(13 downto 12).asUInt
      dBus.cmd.payload.data := dBus.cmd.size.mux (
        U(0) -> input(RS2)(7 downto 0) ## input(RS2)(7 downto 0) ## input(RS2)(7 downto 0) ## input(RS2)(7 downto 0),
        U(1) -> input(RS2)(15 downto 0) ## input(RS2)(15 downto 0),
        default -> input(RS2)(31 downto 0)
      )
      when(arbitration.isValid && input(MEMORY_ENABLE) && !dBus.cmd.ready && !input(ALIGNEMENT_FAULT) && !cmdSent){
        arbitration.haltItself := True
      }

      insert(MEMORY_ADDRESS_LOW) := dBus.cmd.address(1 downto 0)

      //formal
      val formalMask = dBus.cmd.size.mux(
        U(0) -> B"0001",
        U(1) -> B"0011",
        default -> B"1111"
      ) |<< dBus.cmd.address(1 downto 0)
      insert(FORMAL_MEM_ADDR) := dBus.cmd.address & U"xFFFFFFFC"
      insert(FORMAL_MEM_WMASK) := (dBus.cmd.valid &&  dBus.cmd.wr) ? formalMask | B"0000"
      insert(FORMAL_MEM_RMASK) := (dBus.cmd.valid && !dBus.cmd.wr) ? formalMask | B"0000"
      insert(FORMAL_MEM_WDATA) := dBus.cmd.payload.data
    }

    //Collect dBus.rsp read responses
    rspStage plug new Area {
      val s = rspStage; import s._


      insert(MEMORY_READ_DATA) := dBus.rsp.data
      arbitration.haltItself setWhen(arbitration.isValid && input(MEMORY_ENABLE) && !input(INSTRUCTION)(5) && !dBus.rsp.ready)

      if(catchAccessFault || catchAddressMisaligned){
        if(!catchAccessFault){
          memoryExceptionPort.code := (input(INSTRUCTION)(5) ? U(6) | U(4)).resized
          memoryExceptionPort.valid := input(ALIGNEMENT_FAULT)
        } else if(!catchAddressMisaligned){
          memoryExceptionPort.valid := dBus.rsp.ready && dBus.rsp.error && !input(INSTRUCTION)(5)
          memoryExceptionPort.code  := 5
        } else {
          memoryExceptionPort.valid := dBus.rsp.ready && dBus.rsp.error && !input(INSTRUCTION)(5)
          memoryExceptionPort.code  := 5
          when(input(ALIGNEMENT_FAULT)){
            memoryExceptionPort.code := (input(INSTRUCTION)(5) ? U(6) | U(4)).resized
            memoryExceptionPort.valid := True
          }
        }
        when(!(arbitration.isValid && input(MEMORY_ENABLE) && (if(cmdStage == rspStage) !arbitration.isStuckByOthers else True))){
          memoryExceptionPort.valid := False
        }

        memoryExceptionPort.badAddr := input(REGFILE_WRITE_DATA).asUInt  //Drived by IntAluPlugin
      }


      if(rspStage != execute) assert(!(dBus.rsp.ready && input(MEMORY_ENABLE) && arbitration.isValid && arbitration.isStuck),"DBusPlugin doesn't allow memory stage stall when read happend")
    }

    //Reformat read responses, REGFILE_WRITE_DATA overriding
    val injectionStage = if(earlyInjection) memory else stages.last
    injectionStage plug new Area {
      import injectionStage._


      val rspShifted = MEMORY_READ_DATA()
      rspShifted := input(MEMORY_READ_DATA)
      switch(input(MEMORY_ADDRESS_LOW)){
        is(1){rspShifted(7 downto 0) := input(MEMORY_READ_DATA)(15 downto 8)}
        is(2){rspShifted(15 downto 0) := input(MEMORY_READ_DATA)(31 downto 16)}
        is(3){rspShifted(7 downto 0) := input(MEMORY_READ_DATA)(31 downto 24)}
      }

      val rspFormated = input(INSTRUCTION)(13 downto 12).mux(
        0 -> B((31 downto 8) -> (rspShifted(7) && !input(INSTRUCTION)(14)),(7 downto 0) -> rspShifted(7 downto 0)),
        1 -> B((31 downto 16) -> (rspShifted(15) && ! input(INSTRUCTION)(14)),(15 downto 0) -> rspShifted(15 downto 0)),
        default -> rspShifted //W
      )

      when(arbitration.isValid && input(MEMORY_ENABLE)) {
        output(REGFILE_WRITE_DATA) := (if(!onlyLoadWords) rspFormated else input(MEMORY_READ_DATA))
      }

      if(!earlyInjection && !emitCmdInMemoryStage && config.withWriteBackStage)
        assert(!(arbitration.isValid && input(MEMORY_ENABLE) && !input(INSTRUCTION)(5) && arbitration.isStuck),"DBusPlugin doesn't allow writeback stage stall when read happend")

      //formal
      insert(FORMAL_MEM_RDATA) := input(MEMORY_READ_DATA)
    }
  }
}
