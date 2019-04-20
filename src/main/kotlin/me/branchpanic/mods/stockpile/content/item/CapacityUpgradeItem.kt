package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.content.upgrade.CapacityUpgrade
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class CapacityUpgradeItem : Item(Settings()), UpgradeItem {
    override fun getUpgrade(stack: ItemStack): Upgrade = CapacityUpgrade(32)
}
