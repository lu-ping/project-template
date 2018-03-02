package risoc

import chisel3._
import freechips.rocketchip.config.{Parameters, Config}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink.BootROMParams
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.tile.XLen
import testchipip._

class WithBootROM extends Config((site, here, up) => {
  case BootROMParams => BootROMParams(
    //contentFileName = s"./bootrom/bootrom.rv${site(XLen)}.img")
    contentFileName = s"./bootrom/bootrom.img")
})

class WithExampleTop extends Config((site, here, up) => {
  case BuildTop => (clock: Clock, reset: Bool, p: Parameters) =>new ExampleTop()(p)
})

class WithPWM extends Config((site, here, up) => {
  case BuildTop => (clock: Clock, reset: Bool, p: Parameters) => new ExampleTopWithPWM()(p)
})

class WithBlockDeviceModel extends Config((site, here, up) => {
  case BuildTop => (clock: Clock, reset: Bool, p: Parameters) => {
    val top = Module(LazyModule(new ExampleTopWithBlockDevice()(p)).module)
    top.connectBlockDeviceModel()
    top
  }
})

class WithSimBlockDevice extends Config((site, here, up) => {
  case BuildTop => (clock: Clock, reset: Bool, p: Parameters) => {
    val top = Module(LazyModule(new ExampleTopWithBlockDevice()(p)).module)
    top.connectSimBlockDevice(clock, reset)
    top
  }
})




package object Configs {

  implicit class HexHelper(private val sc: StringContext)
    extends AnyVal {
    def rh(args: Any*): Long = {
      val orig = sc
        .s(args: _*)
        .replace("_",
                  "")
      Integer
        .parseInt(orig,
                   16)
    }
  }

}
// use example top + rocketchip default config
// with 512M mem + AXI4 master dram port + bootrom
// 2 ways d$, 8KB

/*class My512MEMPlusPWMConfig
  extends Config(new WithExtMemSize(0x20000000) ++ new DefaultExampleConfig)
*/
/*
  the order of new is important, the precede key/value will override the key/value followed
 */
class My512MEMPlusPWMConfig
  extends Config(new WithExtMemSize(0x20000000) ++
    new WithNExtTopInterrupts(4) ++
    new WithL1DCacheWays(2)++
    new WithBootROMHang(0x10000)++
    new WithPeripheryBusFeq(50000000)++
    new PWMConfig)


class WithPeripheryBusFeq(feq: BigInt) extends Config ((site, here, up) => {
  case PeripheryBusKey => up(PeripheryBusKey, site).copy(frequency = feq)
})

class WithBootROMHang(hang: BigInt) extends Config((site, here, up) => {
  case BootROMParams => up(BootROMParams, site).copy(hang = hang)
})

class BaseExampleConfig extends Config(
  new WithBootROM ++
  new freechips.rocketchip.system.DefaultConfig)

class DefaultExampleConfig extends Config(
  new WithExampleTop ++ new BaseExampleConfig)

class RoccExampleConfig extends Config(
  new WithRoccExample ++ new DefaultExampleConfig)

class PWMConfig extends Config(new WithPWM ++ new BaseExampleConfig)

class SimBlockDeviceConfig extends Config(
  new WithBlockDevice ++ new WithSimBlockDevice ++ new BaseExampleConfig)

class BlockDeviceModelConfig extends Config(
  new WithBlockDevice ++ new WithBlockDeviceModel ++ new BaseExampleConfig)

class WithTwoTrackers extends WithNBlockDeviceTrackers(2)
class WithFourTrackers extends WithNBlockDeviceTrackers(4)

class WithTwoMemChannels extends WithNMemoryChannels(2)
class WithFourMemChannels extends WithNMemoryChannels(4)

class DualCoreConfig extends Config(
  // Core gets tacked onto existing list
  new WithNBigCores(1) ++ new DefaultExampleConfig)

class RV32ExampleConfig extends Config(
  new WithRV32 ++ new DefaultExampleConfig)
