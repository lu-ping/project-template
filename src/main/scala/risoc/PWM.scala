package risoc

import chisel3._
import chisel3.core.withClock
import chisel3.util._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.UIntIsOneOf

case class PWMParams(address: BigInt, beatBytes: Int)

class PWMBase(w: Int) extends Module {
  val io = IO(new Bundle {
    val pwmout = Output(Bool())
    val period = Input(UInt(w.W))
    val duty = Input(UInt(w.W))
    val enable = Input(Bool())
  })

    // The counter should count up until period is reached
    val counter = Reg(UInt(w.W))

    when(counter >= (io.period - 1.U)) {
      counter := 0.U
    }
      .otherwise {
        counter := counter + 1.U
      }

  // If PWM is enabled, pwmout is high when counter < duty
  // If PWM is not enabled, it will always be low
  io.pwmout := io.enable && (counter < io.duty)
}

trait PWMTLBundle extends Bundle {
  val pwmout = Output(Bool())
  val pwmclk=Input(Clock())
}

trait PWMTLModule extends HasRegMap {
  val io: PWMTLBundle
  implicit val p: Parameters
  def params: PWMParams

  val w = params.beatBytes * 8
  require(w <= 64)

  // How many clock cycles in a PWM cycle?
  val period = Reg(UInt(w.W))
  // For how many cycles should the clock be high?
  val duty = Reg(UInt(w.W))
  // Is the PWM even running at all?
  val enable = RegInit(false.B)

  val base = withClock(io.pwmclk)(Module(new PWMBase(w)))
  io.pwmout := base.io.pwmout
  base.io.period := period
  base.io.duty := duty
  base.io.enable := enable

  regmap(
    0x00 -> Seq(
      RegField(w, period)),
    0x08 -> Seq(
      RegField(w, duty)),
    0x10 -> Seq(
      RegField(1, enable)))
}

class PWMTL(c: PWMParams)(implicit p: Parameters)
  extends TLRegisterRouter(
    c.address, "pwm", Seq("ucbbar,pwm"),
    beatBytes = c.beatBytes)(
      new TLRegBundle(c, _) with PWMTLBundle)(new TLRegModule(c, _, _) with PWMTLModule)

trait HasPeripheryPWM extends BaseSubsystem {
  implicit val p: Parameters

  private val address = 0x2000

  val pwm = LazyModule(new PWMTL(
    PWMParams(address, pbus.beatBytes))(p))

  pwm.node := pbus.toVariableWidthSlave()()
}

trait HasPeripheryPWMModuleImp extends LazyModuleImp {
  implicit val p: Parameters
  val outer: HasPeripheryPWM

  val pwmout = IO(new Bundle {
    val clk=Input(Clock())
    val out=Output(Bool())
  })

  pwmout.out := outer.pwm.module.io.pwmout
  outer.pwm.module.io.pwmclk := pwmout.clk
}
