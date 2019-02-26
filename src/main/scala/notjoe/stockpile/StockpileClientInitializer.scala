package notjoe.stockpile

import net.fabricmc.api.{ClientModInitializer, EnvType, Environment}
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity
import notjoe.stockpile.renderer.StockpileBarrelRenderer

@Environment(EnvType.CLIENT)
object StockpileClientInitializer extends ClientModInitializer {
  override def onInitializeClient(): Unit = {
    BlockEntityRendererRegistry.INSTANCE
      .register(classOf[StockpileBarrelBlockEntity], StockpileBarrelRenderer)
  }
}
