package me.branchpanic.mods.stockpile.content.client.renderer

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

class ItemBarrelRenderer : AbstractBarrelRenderer<ItemBarrelBlockEntity, ItemStack>() {
    override fun drawIcon(contents: Quantizer<ItemStack>) {
        MinecraftClient.getInstance().itemRenderer.renderGuiItem(contents.reference, 0, -3)
    }

    override fun shouldSkipRenderingFor(barrel: ItemBarrelBlockEntity): Boolean {
        return barrel.storage.contents.reference.isEmpty
    }
}
