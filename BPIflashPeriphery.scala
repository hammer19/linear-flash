// See LICENSE for license details.
package sifive.blocks.devices.xilinxvc707BPIflash

import Chisel._
import diplomacy._
import coreplex.BankedL2Config
import config._
import uncore.tilelink2._
import rocketchip._

trait PeripheryBPIflash extends TopNetwork {
  
  val module: PeripheryBPIflashModule

  val BPIflash = LazyModule(new BPI_flash)
 // val flash = Seq(BPIflash.node)
  BPIflash.node := TLWidthWidget(socBusConfig.beatBytes)(socBus.node)
  
}

trait PeripheryBPIflashBundle extends TopNetworkBundle {
  val BPIflash = new BPI_flashIO
}

trait PeripheryBPIflashModule extends TopNetworkModule {
  val outer: PeripheryBPIflash
  val io: PeripheryBPIflashBundle

  io.BPIflash <> outer.BPIflash.module.io.port
}
  
  