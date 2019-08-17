package me.branchpanic.mods.stockpile.content.client.renderer

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import me.branchpanic.mods.stockpile.content.blockentity.PotionBarrelBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionUtil

class PotionBarrelRenderer : AbstractBarrelRenderer<PotionBarrelBlockEntity, Potion>() {
    override fun shouldSkipRenderingFor(barrel: PotionBarrelBlockEntity): Boolean = false

    override fun drawIcon(contents: Quantizer<Potion>) {
        MinecraftClient.getInstance().itemRenderer.renderGuiItem(
            PotionUtil.setPotion(
                ItemStack(Items.POTION),
                contents.reference
            ), 0, -3
        )
    }
}
