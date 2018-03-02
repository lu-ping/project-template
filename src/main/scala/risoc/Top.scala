package risoc

import chisel3._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util.DontTouch
import freechips.rocketchip.devices.debug._
import testchipip._

class ExampleTop(implicit p: Parameters) extends RocketSubsystem
    with HasAsyncExtInterrupts
    with HasMasterAXI4MemPort
    with HasMasterAXI4MMIOPort
    with HasPeripheryBootROM
    with HasSystemErrorSlave
    with HasPeripheryDebug
    {
  override lazy val module = new ExampleTopModule(this)
}

class ExampleTopModule[+L <: ExampleTop](l: L) extends RocketSubsystemModuleImp(l)
    with HasExtInterruptsModuleImp
    with HasRTCModuleImp
    with HasMasterAXI4MemPortModuleImp
    with HasMasterAXI4MMIOPortModuleImp
    with HasPeripheryBootROMModuleImp
    with HasPeripheryDebugModuleImp
    with DontTouch

class ExampleTopWithPWM(implicit p: Parameters) extends ExampleTop
    with HasPeripheryPWM {
  override lazy val module = new ExampleTopWithPWMModule(this)
}

class ExampleTopWithPWMModule(l: ExampleTopWithPWM)
  extends ExampleTopModule(l) with HasPeripheryPWMModuleImp

class ExampleTopWithBlockDevice(implicit p: Parameters) extends ExampleTop
    with HasPeripheryBlockDevice {
  override lazy val module = new ExampleTopWithBlockDeviceModule(this)
}

class ExampleTopWithBlockDeviceModule(l: ExampleTopWithBlockDevice)
  extends ExampleTopModule(l)
  with HasPeripheryBlockDeviceModuleImp
