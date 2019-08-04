package me.branchpanic.mods.stockpile.api

import alexiil.mc.lib.attributes.Attribute
import alexiil.mc.lib.attributes.Attributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

interface BarrelHatInteraction {
    companion object {
        @JvmStatic
        val BARREL_HAT_INTERACTION: Attribute<BarrelHatInteraction> =
            Attributes.create(BarrelHatInteraction::class.java)
    }

    fun pushContents(toStack: ItemStack, player: PlayerEntity)
    fun pullContents(fromStack: ItemStack, player: PlayerEntity)
}
