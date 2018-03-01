
  implicit class QHelper(val sc : StringContext) {
    def Q(args : Any*): Seq[String] = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      var buf = new StringBuffer(strings.next)
      while(strings.hasNext) {
        buf append expressions.next
        buf append strings.next
      }

      buf.toString.split("\n")
        .toSeq
        .map(_.trim)
        .filter(_ != "")
    }
  }
    val list=Q"""
                |  // Inouts
                |  inout [15:0]       ddr3_dq,
                |  inout [1:0]        ddr3_dqs_n,
                |  inout [1:0]        ddr3_dqs_p,
                |  // Outputs
                |  output [14:0]     ddr3_addr,
                |  output [2:0]        ddr3_ba,
                |  output            ddr3_ras_n,
                |  output            ddr3_cas_n,
                |  output            ddr3_we_n,
                |  output            ddr3_reset_n,
                |  output [0:0]       ddr3_ck_p,
                |  output [0:0]       ddr3_ck_n,
                |  output [0:0]       ddr3_cke,
                |  output [1:0]     ddr3_dm,
                |  output [0:0]       ddr3_odt,
                |  // Inputs
                |  // Single-ended system clock
                |  input             sys_clk_i,
                |  // user interface signals
                |  output            ui_clk,
                |  output            ui_clk_sync_rst,
                |  output            mmcm_locked,
                |  input         aresetn,
                |  input         app_sr_req,
                |  input         app_ref_req,
                |  input         app_zq_req,
                |  output            app_sr_active,
                |  output            app_ref_ack,
                |  output            app_zq_ack,
                |  // Slave Interface Write Address Ports
                |  input [3:0]           s_axi_awid,
                |  input [28:0]         s_axi_awaddr,
                |  input [7:0]           s_axi_awlen,
                |  input [2:0]           s_axi_awsize,
                |  input [1:0]           s_axi_awburst,
                |  input [0:0]           s_axi_awlock,
                |  input [3:0]           s_axi_awcache,
                |  input [2:0]           s_axi_awprot,
                |  input [3:0]           s_axi_awqos,
                |  input         s_axi_awvalid,
                |  output            s_axi_awready,
                |  // Slave Interface Write Data Ports
                |  input [63:0]         s_axi_wdata,
                |  input [7:0]         s_axi_wstrb,
                |  input         s_axi_wlast,
                |  input         s_axi_wvalid,
                |  output            s_axi_wready,
                |  // Slave Interface Write Response Ports
                |  input         s_axi_bready,
                |  output [3:0]          s_axi_bid,
                |  output [1:0]          s_axi_bresp,
                |  output            s_axi_bvalid,
                |  // Slave Interface Read Address Ports
                |  input [3:0]           s_axi_arid,
                |  input [28:0]         s_axi_araddr,
                |  input [7:0]           s_axi_arlen,
                |  input [2:0]           s_axi_arsize,
                |  input [1:0]           s_axi_arburst,
                |  input [0:0]           s_axi_arlock,
                |  input [3:0]           s_axi_arcache,
                |  input [2:0]           s_axi_arprot,
                |  input [3:0]           s_axi_arqos,
                |  input         s_axi_arvalid,
                |  output            s_axi_arready,
                |  // Slave Interface Read Data Ports
                |  input         s_axi_rready,
                |  output [3:0]          s_axi_rid,
                |  output [63:0]            s_axi_rdata,
                |  output [1:0]          s_axi_rresp,
                |  output            s_axi_rlast,
                |  output            s_axi_rvalid,
                |  output            init_calib_complete,
                |  output [11:0]                                device_temp,
                |  input			sys_rst
      """
  val mypat1=raw"^\s*(inout|input|output)\s+\[([0-9]+)\:[0-9]+\]\s+(\w+),?".r
  val mypat2=raw"^\s*(inout|input|output)\s+(\w+),?".r
  val mypat3=raw"^(//.*)".r
  list.map(_.replace("|","").trim).filter(_ match {
    case mypat1(_,_,_) => true
    case mypat2(_,_) => true
    case mypat3(_) => true
    case _ => false
  }).map(_ match {
    case mypat1(direction,width,name) => if(direction=="inout") s"val ${name}In = Input(UInt(${(width.toInt + 1).toString}.W))\nval ${name}Out = Output(UInt(${(width.toInt + 1).toString}.W))" else s"val ${name} = ${direction.capitalize}(UInt(${(width.toInt + 1).toString}.W))"
    case mypat2(direction,name) => if(direction=="inout") s"val ${name}In = Input(UInt(1.W))\nval ${name}Out = Output(UInt(1.W))" else s"val $name = ${direction.capitalize}(UInt(1.W))"
    case mypat3(comments) => comments}) foreach println
