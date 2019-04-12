package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.renderer.ItemBarrelRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry

@Environment(EnvType.CLIENT)
object StockpileClient : ClientModInitializer {
    override fun onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(ItemBarrelBlockEntity::class.java, ItemBarrelRenderer)
    }
}
