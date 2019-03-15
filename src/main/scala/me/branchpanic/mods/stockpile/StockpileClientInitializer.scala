package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.blockentity.StockpileBarrelBlockEntity
import me.branchpanic.mods.stockpile.renderer.StockpileBarrelRenderer
import net.fabricmc.api.{ClientModInitializer, EnvType, Environment}
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry

@Environment(EnvType.CLIENT)
class StockpileClientInitializer extends ClientModInitializer {
  override def onInitializeClient(): Unit = {
    BlockEntityRendererRegistry.INSTANCE
      .register(classOf[StockpileBarrelBlockEntity], StockpileBarrelRenderer)
  }
}
