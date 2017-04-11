// See LICENSE for license details.
package sifive.blocks.ip.xilinx.axi_emc_bpiflash

import Chisel._
import config._
import diplomacy._
import uncore.axi4._
import junctions._

// IP VLNV: xilinx.com:customize_ip:AXI_EMC:1.0
// Black Box

trait AXIToBPIIOPhysical extends Bundle {
  //physical external pins
  // bidirectional signals on blackbox interface
  // defined here as an output so "__inout" signal name does not have to be used
  // verilog does not check the
    val FLASH_A                   = Bits(OUTPUT,26)
    val FLASH_D                   = Bits(OUTPUT,16)
    val FLASH_FWE_B               = Bool(OUTPUT)
    val FLASH_OE_B                = Bool(OUTPUT)
    val FLASH_CE_B                = Bool(OUTPUT)
    val FLASH_ADV_B               = Bool(OUTPUT)
}

trait AXIToBPIIOClocksReset extends Bundle {
  //clock, reset, control
     val bpi_axi_aclk             = Clock(INPUT)
     val bpi_axi_aresetn          = Bool(INPUT)
     val rdclk                    = Clock(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module 
class AXI_EMC_BPIflash(implicit val p:Parameters) extends BlackBox
{
  val io = new Bundle with AXIToBPIIOPhysical
                      with AXIToBPIIOClocksReset{
    //axi_s
    //slave interface write address ports
    val bpi_s_axi_awid            = Bits(INPUT,4)
    val bpi_s_axi_awaddr          = Bits(INPUT,30)
    val bpi_s_axi_awlen           = Bits(INPUT,8)
    val bpi_s_axi_awsize          = Bits(INPUT,3)
    val bpi_s_axi_awburst         = Bits(INPUT,2)
    val bpi_s_axi_awlock          = Bits(INPUT,1)
    val bpi_s_axi_awcache         = Bits(INPUT,4)
    val bpi_s_axi_awprot          = Bits(INPUT,3)
    val bpi_s_axi_awvalid         = Bool(INPUT)
    val bpi_s_axi_awready         = Bool(OUTPUT)
    //slave interface write data ports
    val bpi_s_axi_wdata           = Bits(INPUT,64)
    val bpi_s_axi_wstrb           = Bits(INPUT,8)
    val bpi_s_axi_wlast           = Bool(INPUT)
    val bpi_s_axi_wvalid          = Bool(INPUT)
    val bpi_s_axi_wready          = Bool(OUTPUT)
    //slave interface write response ports
    val bpi_s_axi_bready          = Bool(INPUT)
    val bpi_s_axi_bid             = Bits(OUTPUT,4)
    val bpi_s_axi_bresp           = Bits(OUTPUT,2)
    val bpi_s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address ports
    val bpi_s_axi_arid            = Bits(INPUT,4)
    val bpi_s_axi_araddr          = Bits(INPUT,30)
    val bpi_s_axi_arlen           = Bits(INPUT,8)
    val bpi_s_axi_arsize          = Bits(INPUT,3)
    val bpi_s_axi_arburst         = Bits(INPUT,2)
    val bpi_s_axi_arlock          = Bits(INPUT,1)
    val bpi_s_axi_arcache         = Bits(INPUT,4)
    val bpi_s_axi_arprot          = Bits(INPUT,3)
    val bpi_s_axi_arvalid         = Bool(INPUT)
    val bpi_s_axi_arready         = Bool(OUTPUT)
    //slave interface read data ports
    val bpi_s_axi_rready          = Bool(INPUT)
    val bpi_s_axi_rid             = Bits(OUTPUT,4)
    val bpi_s_axi_rdata           = Bits(OUTPUT,64)
    val bpi_s_axi_rresp           = Bits(OUTPUT,2)
    val bpi_s_axi_rlast           = Bool(OUTPUT)
    val bpi_s_axi_rvalid          = Bool(OUTPUT)
  }
}

//wrap axi_to_bpi_flash black box in Nasti Bundles

class AXIToBPIFLASH(implicit p:Parameters) extends LazyModule
{
  val slave = AXI4SlaveNode(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x30000000L, 0x07ffffffL)),
      executable    = true,
      supportsWrite = TransferSizes(1, 256),
      supportsRead  = TransferSizes(1, 256),
      interleavedId = Some(0))), // the Xilinx IP is friendly
    beatBytes = 8))


  lazy val module = new LazyModuleImp(this) {
    // Must have exactly the right number of idBits
    require (slave.edgesIn(0).bundle.idBits == 4)

    class AXIToBPIIOBundle extends Bundle with AXIToBPIIOPhysical
                                          with AXIToBPIIOClocksReset;

    val io = new Bundle {
      val port = new AXIToBPIIOBundle
      val slave_in = slave.bundleIn
    }

    val blackbox = Module(new AXI_EMC_BPIflash)

    val s = io.slave_in(0)

    //to top level
    blackbox.io.bpi_axi_aclk        := io.port.bpi_axi_aclk
	blackbox.io.bpi_axi_aresetn     := io.port.bpi_axi_aresetn
	blackbox.io.rdclk               := io.port.rdclk
    io.port.FLASH_A                 := blackbox.io.FLASH_A
    io.port.FLASH_D                 := blackbox.io.FLASH_D
    io.port.FLASH_FWE_B             := blackbox.io.FLASH_FWE_B
    io.port.FLASH_OE_B              := blackbox.io.FLASH_OE_B
    io.port.FLASH_CE_B              := blackbox.io.FLASH_CE_B
	io.port.FLASH_ADV_B           	:= blackbox.io.FLASH_ADV_B

    //s
    //AXI4 signals ordered as per AXI4 Specification (Release D) Section A.2
    //-{lock, cache, prot, qos} 
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //slave interface write address
    blackbox.io.bpi_s_axi_awid      := s.aw.bits.id
    blackbox.io.bpi_s_axi_awaddr    := s.aw.bits.addr
    blackbox.io.bpi_s_axi_awlen     := s.aw.bits.len
    blackbox.io.bpi_s_axi_awsize    := s.aw.bits.size
    blackbox.io.bpi_s_axi_awburst   := s.aw.bits.burst
    blackbox.io.bpi_s_axi_awlock    := s.aw.bits.lock
    blackbox.io.bpi_s_axi_awcache   := s.aw.bits.cache
    blackbox.io.bpi_s_axi_awprot    := s.aw.bits.prot
   // blackbox.io.bpi_s_axi_awqos     := s.aw.bits.qos
  //  blackbox.io.bpi_s_axi_awregion  := UInt(0)
    //blackbox.io.awuser            := s.aw.bits.user
    blackbox.io.bpi_s_axi_awvalid       := s.aw.valid
    s.aw.ready                      := blackbox.io.bpi_s_axi_awready
    //slave interface write data ports
    //blackbox.io.bpi_s_axi_wid     := s.w.bits.id
    blackbox.io.bpi_s_axi_wdata     := s.w.bits.data
    blackbox.io.bpi_s_axi_wstrb     := s.w.bits.strb
    blackbox.io.bpi_s_axi_wlast     := s.w.bits.last
    //blackbox.io.bpi_s_axi_wuser   := s.w.bits.user
    blackbox.io.bpi_s_axi_wvalid    := s.w.valid
    s.w.ready                       := blackbox.io.bpi_s_axi_wready
    //slave interface write response
    s.b.bits.id                     := blackbox.io.bpi_s_axi_bid
    s.b.bits.resp                   := blackbox.io.bpi_s_axi_bresp
    //s.b.bits.user                 := blackbox.io.bpi_s_axi_buser
    s.b.valid                       := blackbox.io.bpi_s_axi_bvalid
    blackbox.io.bpi_s_axi_bready    := s.b.ready
    //slave AXI interface read address ports
    blackbox.io.bpi_s_axi_arid      := s.ar.bits.id
    blackbox.io.bpi_s_axi_araddr    := s.ar.bits.addr
    blackbox.io.bpi_s_axi_arlen     := s.ar.bits.len
    blackbox.io.bpi_s_axi_arsize    := s.ar.bits.size
    blackbox.io.bpi_s_axi_arburst   := s.ar.bits.burst
    blackbox.io.bpi_s_axi_arlock    := s.ar.bits.lock
    blackbox.io.bpi_s_axi_arcache   := s.ar.bits.cache
    blackbox.io.bpi_s_axi_arprot    := s.ar.bits.prot
    //blackbox.io.bpi_s_axi_arqos     := s.ar.bits.qos
    //blackbox.io.bpi_s_axi_arregion  := UInt(0)
    //blackbox.io.bpi_s_axi_aruser  := s.ar.bits.user
    blackbox.io.bpi_s_axi_arvalid   := s.ar.valid
    s.ar.ready                      := blackbox.io.bpi_s_axi_arready
    //slave AXI interface read data ports
    s.r.bits.id                     := blackbox.io.bpi_s_axi_rid
    s.r.bits.data                   := blackbox.io.bpi_s_axi_rdata
    s.r.bits.resp                   := blackbox.io.bpi_s_axi_rresp
    s.r.bits.last                   := blackbox.io.bpi_s_axi_rlast
    //s.r.bits.ruser                := blackbox.io.bpi_s_axi_ruser
    s.r.valid                       := blackbox.io.bpi_s_axi_rvalid
    blackbox.io.bpi_s_axi_rready    := s.r.ready

  }
}
