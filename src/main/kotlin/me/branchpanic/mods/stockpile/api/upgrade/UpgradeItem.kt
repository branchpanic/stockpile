package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.item.ItemStack

/**
 * An UpgradeItem provides an Upgrade from an ItemStack.
 *
 * Items implementing UpgradeItem need not implement "right-click to upgrade" functionality, as Stockpile takes care
 * of this automatically.
 */
interface UpgradeItem {
    fun getUpgrade(stack: ItemStack): Upgrade
}