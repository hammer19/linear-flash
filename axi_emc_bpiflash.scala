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

