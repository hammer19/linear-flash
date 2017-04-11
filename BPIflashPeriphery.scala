// See LICENSE for license details.
package sifive.blocks.devices.xilinxvc707BPIflash


import Chisel._
import diplomacy.LazyModule
import rocketchip.{L2Crossbar,L2CrossbarModule,L2CrossbarBundle}
import uncore.tilelink2.TLWidthWidget

trait PeripheryBPIflash extends L2Crossbar {

  val BPIflash = LazyModule(new BPI_flash)
 BPIflash.slave   := TLWidthWidget(socBusConfig.beatBytes)(socBus.node)
}

trait PeripheryBPIflashBundle extends L2CrossbarBundle {
  val BPIflash = new BPI_flashIO
}

trait PeripheryBPIflashModule extends L2CrossbarModule {
  val outer: PeripheryBPIflash
  val io: PeripheryBPIflashBundle

  io.BPIflash <> outer.BPIflash.module.io.port
}