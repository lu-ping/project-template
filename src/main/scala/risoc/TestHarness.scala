package risoc

import chisel3._
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.system.Generator.names
import freechips.rocketchip.util.GeneratorApp
import ip._

case object BuildTop extends Field[(Clock, Bool, Parameters) => ExampleTopWithPWM]

class TestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val dut = Module(LazyModule(p(BuildTop)(clock, reset.toBool, p)).module)
  dut.reset := reset.toBool() | dut.debug.ndreset
  dut.tieOffInterrupts()
  dut.connectDebug(clock,reset.toBool(),io.success)
  dut.connectSimAXIMMIO()
  dut.connectSimAXIMem()
  dut.pwmout.clk := clock
  //val mig_wrapper = LazyModule(new mig_7series_0_wrapper_lazymodule)
  //Module(mig_wrapper.module).io <> dut.mem_axi4(0)
  // after all Module constructed
  dut.dontTouchPorts()
}

object Generator extends GeneratorApp {
  val longName = names.topModuleProject + "." + names.configs
  generateFirrtl
  generateAnno
  generateROMs
  generateArtefacts
}
