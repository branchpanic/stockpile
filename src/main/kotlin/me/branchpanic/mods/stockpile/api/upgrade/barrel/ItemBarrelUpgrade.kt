package me.branchpanic.mods.stockpile.api.upgrade.barrel

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade

interface ItemBarrelUpgrade : Upgrade {
    fun upgradeMaxStacks(currentMaxStacks: Int): Int
}