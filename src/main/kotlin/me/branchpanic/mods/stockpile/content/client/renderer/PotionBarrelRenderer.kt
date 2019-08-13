package me.branchpanic.mods.stockpile.content.client.renderer

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import me.branchpanic.mods.stockpile.content.blockentity.PotionBarrelBlockEntity
import net.minecraft.potion.Potion

class PotionBarrelRenderer : AbstractBarrelRenderer<PotionBarrelBlockEntity, Potion>() {
    override fun shouldSkipRenderingFor(barrel: PotionBarrelBlockEntity): Boolean = false

    override fun drawIcon(contents: Quantizer<Potion>) {
        println("poition")
    }
}
