package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.client.BarrelHatKeyListener
import me.branchpanic.mods.stockpile.content.client.renderer.ItemBarrelRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.event.client.ClientTickCallback
import net.minecraft.client.util.InputUtil

@Environment(EnvType.CLIENT)
object StockpileClient : ClientModInitializer {
    val BARREL_HAT_KEY: FabricKeyBinding = FabricKeyBinding.Builder.create(
        Stockpile.id("barrel_hat"),
        InputUtil.Type.KEYSYM,
        InputUtil.fromName("key.keyboard.g").keyCode,
        "controls.stockpile"
    ).build()

    override fun onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(ItemBarrelBlockEntity::class.java, ItemBarrelRenderer)

        KeyBindingRegistry.INSTANCE.addCategory("controls.stockpile")
        KeyBindingRegistry.INSTANCE.register(BARREL_HAT_KEY)

        ClientTickCallback.EVENT.register(BarrelHatKeyListener)
    }
}
