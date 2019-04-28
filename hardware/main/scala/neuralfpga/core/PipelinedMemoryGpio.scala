package neuralfpga.core


import spinal.lib.bus.simple.{PipelinedMemoryBus, PipelinedMemoryBusConfig, PipelinedMemoryBusSlaveFactory}
import spinal.lib.io.Gpio

case class PipelinedMemoryGpio (  parameter: Gpio.Parameter,
                                  busConfig: PipelinedMemoryBusConfig = PipelinedMemoryBusConfig(12, 32)
                               ) extends Gpio.Ctrl[PipelinedMemoryBus] (
  parameter,
  PipelinedMemoryBus(busConfig),
  new PipelinedMemoryBusSlaveFactory(_)
) { val dummy = 0 }
