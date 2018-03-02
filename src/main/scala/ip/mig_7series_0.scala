package ip

import chisel3._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.util.HeterogeneousBag

trait mig_7series_0_axi4_bundle {
  // Slave Interface Write Address Ports
  val s_axi_awid = Input(UInt(4.W))
  val s_axi_awaddr = Input(UInt(29.W))
  val s_axi_awlen = Input(UInt(8.W))
  val s_axi_awsize = Input(UInt(3.W))
  val s_axi_awburst = Input(UInt(2.W))
  val s_axi_awlock = Input(Bool())
  val s_axi_awcache = Input(UInt(4.W))
  val s_axi_awprot = Input(UInt(3.W))
  val s_axi_awqos = Input(UInt(4.W))
  val s_axi_awvalid = Input(Bool())
  val s_axi_awready = Output(Bool())
  // Slave Interface Write Data Ports
  val s_axi_wdata = Input(UInt(64.W))
  val s_axi_wstrb = Input(UInt(8.W))
  val s_axi_wlast = Input(Bool())
  val s_axi_wvalid = Input(Bool())
  val s_axi_wready = Output(Bool())
  // Slave Interface Write Response Ports
  val s_axi_bready = Input(Bool())
  val s_axi_bid = Output(UInt(4.W))
  val s_axi_bresp = Output(UInt(2.W))
  val s_axi_bvalid = Output(Bool())
  // Slave Interface Read Address Ports
  val s_axi_arid = Input(UInt(4.W))
  val s_axi_araddr = Input(UInt(29.W))
  val s_axi_arlen = Input(UInt(8.W))
  val s_axi_arsize = Input(UInt(3.W))
  val s_axi_arburst = Input(UInt(2.W))
  val s_axi_arlock = Input(Bool())
  val s_axi_arcache = Input(UInt(4.W))
  val s_axi_arprot = Input(UInt(3.W))
  val s_axi_arqos = Input(UInt(4.W))
  val s_axi_arvalid = Input(Bool())
  val s_axi_arready = Output(Bool())
  // Slave Interface Read Data Ports
  val s_axi_rready = Input(Bool())
  val s_axi_rid = Output(UInt(4.W))
  val s_axi_rdata = Output(UInt(64.W))
  val s_axi_rresp = Output(UInt(2.W))
  val s_axi_rlast = Output(Bool())
  val s_axi_rvalid = Output(Bool())

  val aresetn = Input(Bool())
}
trait mig_7series_0_ui_bundle {
  // user interface signals
  val ui_clk = Output(Bool())
  val ui_clk_sync_rst = Output(Bool())
  val mmcm_locked = Output(Bool())
  val app_sr_req = Input(Bool())
  val app_sr_active = Output(Bool())
  val app_ref_req = Input(Bool())
  val app_ref_ack = Output(Bool())
  val app_zq_req = Input(Bool())
  val app_zq_ack = Output(Bool())
}

trait mig_7series_0_ddr3_single_dir_bundle {
  // Outputs
  val ddr3_addr = Output(UInt(15.W))
  val ddr3_ba = Output(UInt(3.W))
  val ddr3_ras_n = Output(Bool())
  val ddr3_cas_n = Output(Bool())
  val ddr3_we_n = Output(Bool())
  val ddr3_reset_n = Output(Bool())
  val ddr3_ck_p = Output(Bool())
  val ddr3_ck_n = Output(Bool())
  val ddr3_cke = Output(Bool())
  val ddr3_dm = Output(UInt(2.W))
  val ddr3_odt = Output(Bool())
  // fpga output mig design do not contain ddr3_cs_n and ddr3_reset_n, because chip select and reset has been
  // connected to ddr3 by fpga board.
}
trait  mig_7series_0_ddr3_inout_bundle{
  /*TODO, Inouts  port, do not connect them for automatically creating connection by chisel. need manually change the wiring from in/out to inout pin here. not capable to run simulation backend.
 */
  /* inout, no need to connect this bundle as fpga board will route mig design ddr3 output port to external onboard ddr3
  val ddr3_dq = Inout(UInt(16.W))
  val ddr3_dqs_n = Inout(UInt(2.W))
  val ddr3_dqs_p = Inout(UInt(2.W))
  */
  // Inouts -> input/output
  val ddr3_dq = Input(UInt(16.W))
  val ddr3_dqOut = Output(UInt(16.W))
  val ddr3_dqs_nIn = Input(UInt(2.W))
  val ddr3_dqs_nOut = Output(UInt(2.W))
  val ddr3_dqs_pIn = Input(UInt(2.W))
  val ddr3_dqs_pOut = Output(UInt(2.W))
}

class mig_7series_0_fpga_side_bundle extends Bundle
    with mig_7series_0_ui_bundle
    with mig_7series_0_axi4_bundle
{
  // Inputs
  // Single-ended system clock
  val sys_clk_i = Input(Bool())

  val init_calib_complete = Output(Bool())
  val device_temp = Output(UInt(12.W))
  val sys_rst = Input(Bool())
}

class mig_7series_0 extends BlackBox with HasBlackBoxResource{
  val io = IO(new mig_7series_0_fpga_side_bundle)
  setResource("/mig_7series_0.v")
}

class mig_7series_0_wrapper_lazymodule(implicit p: Parameters) extends LazyModule {

  lazy val module=new mig_7series_0_wrapper(this)
}

class mig_7series_0_wrapper(_outer: mig_7series_0_wrapper_lazymodule) extends LazyModuleImp(_outer) {
  val io = IO(Flipped(AXI4Bundle(AXI4BundleParameters(
                                               addrBits=32,
                                               dataBits=64,
                                               idBits=4,
                                               userBits=0))))
  val mig=Module(new mig_7series_0)
  connectMC()
  def connectMC() = {
    // write address
    io.aw.ready := mig.io.s_axi_awready
    mig.io.s_axi_awvalid := io.aw.valid
    mig.io.s_axi_awaddr := io.aw.bits.addr(28,0)
    mig.io.s_axi_awburst := io.aw.bits.burst
    mig.io.s_axi_awid := io.aw.bits.id
    mig.io.s_axi_awlen := io.aw.bits.len
    mig.io.s_axi_awsize := io.aw.bits.size
    mig.io.s_axi_awlock := io.aw.bits.lock
    mig.io.s_axi_awcache := io.aw.bits.cache
    mig.io.s_axi_awprot := io.aw.bits.prot
    mig.io.s_axi_awqos := io.aw.bits.qos
    // write data
    io.w.ready := mig.io.s_axi_wready
    mig.io.s_axi_wvalid := io.w.valid
    mig.io.s_axi_wdata := io.w.bits.data
    mig.io.s_axi_wlast := io.w.bits.last
    mig.io.s_axi_wstrb := io.w.bits.strb
    // write response
    mig.io.s_axi_bready := io.b.ready
    io.b.valid := mig.io.s_axi_bvalid
    io.b.bits.resp := mig.io.s_axi_bresp
    io.b.bits.id := mig.io.s_axi_bid
    // read address
    io.ar.ready := mig.io.s_axi_arready
    mig.io.s_axi_arvalid := io.ar.valid
    mig.io.s_axi_araddr := io.ar.bits.addr(28,0)
    mig.io.s_axi_arid := io.ar.bits.id
    mig.io.s_axi_arlen := io.ar.bits.len
    mig.io.s_axi_arsize := io.ar.bits.size
    mig.io.s_axi_arburst := io.ar.bits.burst
    mig.io.s_axi_arlock := io.ar.bits.lock
    mig.io.s_axi_arcache := io.ar.bits.cache
    mig.io.s_axi_arprot := io.ar.bits.prot
    mig.io.s_axi_arqos := io.ar.bits.qos
    //read data
    mig.io.s_axi_rready := io.r.ready
    io.r.valid := mig.io.s_axi_rvalid
    io.r.bits.data := mig.io.s_axi_rdata
    io.r.bits.id := mig.io.s_axi_rid
    io.r.bits.resp := mig.io.s_axi_rresp
    io.r.bits.last := mig.io.s_axi_rlast
    //ui
    mig.io.app_sr_req := 0.U
    mig.io.app_ref_req := 0.U
    mig.io.app_zq_req := 0.U
    //clock & reset
    //mig.io.sys_clk_i := clock
    mig.io.sys_rst := ~reset.asUInt()

    // not use
    //mig.io.init_calib_complete
    //mig.io.device_temp
    io
  }


}