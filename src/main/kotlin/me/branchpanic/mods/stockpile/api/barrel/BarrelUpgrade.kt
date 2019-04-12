package me.branchpanic.mods.stockpile.api.barrel

import net.minecraft.text.TextComponent
import net.minecraft.util.Identifier

/**
 * A BarrelUpgrade changes the behavior of an item barrel in some way, such as by increasing the maximum number of
 * stacks it can hold.
 *
 * BarrelUpgrades must be registered through [BarrelUpgrades.register] in order for them to be loaded and saved
 * properly.
 */
interface BarrelUpgrade {

    /**
     * The unique identifier of this BarrelUpgrade, which should correspond to one registered with
     * [BarrelUpgrades.register].
     */
    val id: Identifier

    /**
     * A brief, user-facing description of this BarrelUpgrade.
     */
    val description: TextComponent

    /**
     * Determines whether or not this BarrelUpgrade can be applied to a barrel containing other existing upgrades.
     */
    fun isCompatibleWith(otherUpgrades: List<BarrelUpgrade>): Boolean

    /**
     * Returns the upgraded capacity of a barrel with this upgrade applied, given the capacity before applying this
     * upgrade.
     */
    fun upgradeCapacity(currentCapacityStacks: Int): Int
}