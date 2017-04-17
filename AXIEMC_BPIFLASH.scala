// See LICENSE for license details.
package sifive.blocks.devices.xilinxvc707BPIflash

import Chisel._
import config._
import diplomacy._
import uncore.tilelink2._
import uncore.axi4._
import rocketchip._
import sifive.blocks.ip.xilinx.axi_emc_bpiflash.{AXI_EMC_BPIflash, AXIToBPIIOClocksReset, AXIToBPIIOPhysical}
import sifive.blocks.ip.xilinx.ibufds_gte2.IBUFDS_GTE2

class BPI_flashPads extends Bundle with AXIToBPIIOPhysical

class BPI_flashIO extends Bundle with AXIToBPIIOPhysical
                                         with AXIToBPIIOClocksReset 


class BPI_flash(implicit p:Parameters) extends LazyModule
{
  val node = TLInputNode()
  val axi4 = AXI4InternalOutputNode(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x30000000L, 0x07ffffffL)),
      executable    = true,
      supportsWrite = TransferSizes(1, 256),
      supportsRead  = TransferSizes(1, 256),
      interleavedId = Some(0))), // the Xilinx IP is friendly
    beatBytes = 8))
    val xing = LazyModule(new TLAsyncCrossing)
    val toaxi4 = LazyModule(new TLToAXI4(idBits = 4))

    xing.node := node
    val monitor = (toaxi4.node := xing.node)
    axi4 := toaxi4.node

  lazy val module = new LazyModuleImp(this) {
    // Must have exactly the right number of idBits

    class AXIToBPIIOBundle extends Bundle with AXIToBPIIOPhysical
                                          with AXIToBPIIOClocksReset;

    val io = new Bundle {
      val port = new AXIToBPIIOBundle
      val slave_in = node.bundleIn
    }

    val blackbox = Module(new AXI_EMC_BPIflash)

    val s = axi4.bundleIn(0)
	xing.module.io.in_clock := clock
    xing.module.io.in_reset := reset
    xing.module.io.out_clock := io.port.bpi_axi_aclk
    xing.module.io.out_reset := ~(io.port.bpi_axi_aresetn)
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
