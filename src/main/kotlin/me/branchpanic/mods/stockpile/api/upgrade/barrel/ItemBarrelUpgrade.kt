package me.branchpanic.mods.stockpile.api.upgrade.barrel

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade

/**
 * An ItemBarrelUpgrade upgrades the functionality of an Item Barrel.
 */
interface ItemBarrelUpgrade : Upgrade {

    /**
     * Modifies the maximum number of stacks that this Item Barrel can hold. **Reducing the given value may result in
     * deleted items**.
     */
    fun upgradeMaxStacks(currentMaxStacks: Int): Int = currentMaxStacks
}