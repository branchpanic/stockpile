package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.item.ItemStack

interface UpgradeItem {
    fun getUpgrade(stack: ItemStack): Upgrade
}