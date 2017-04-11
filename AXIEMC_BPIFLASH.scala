// See LICENSE for license details.
package sifive.blocks.devices.xilinxvc707BPIflash

import Chisel._
import config._
import diplomacy._
import uncore.tilelink2._
import uncore.axi4._
import rocketchip._
import sifive.blocks.ip.xilinx.axi_emc_bpiflash.{AXIToBPIFLASH, AXIToBPIIOClocksReset, AXIToBPIIOPhysical}
import sifive.blocks.ip.xilinx.ibufds_gte2.IBUFDS_GTE2


class BPI_flashPads extends Bundle with AXIToBPIIOPhysical

class BPI_flashIO extends Bundle with AXIToBPIIOPhysical
                                         with AXIToBPIIOClocksReset 

class BPI_flash(implicit p: Parameters) extends LazyModule {
  val slave = TLInputNode()
  val axi_to_bpi_flash = LazyModule(new AXIToBPIFLASH)
  axi_to_bpi_flash.slave   := TLToAXI4(idBits=4)(slave)

  lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val port = new BPI_flashIO
      val slave_in = slave.bundleIn
    }

    io.port <> axi_to_bpi_flash.module.io.port
  }

}
