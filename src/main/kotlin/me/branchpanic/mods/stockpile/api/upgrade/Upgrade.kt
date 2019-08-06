package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

interface Upgrade {

    /**
     * The Identifier tying this Upgrade to an [UpgradeType].
     */
    val id: Identifier

    /**
     * The description of this Upgrade, often used in tooltips.
     */
    val description: Text

    /**
     * Determines which upgrades in a given list conflict with this Upgrade.
     */
    fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> = emptyList()

    /**
     * Gets an ItemStack representing this upgrade, used when removing upgrades with the Screwdriver.
     */
    fun toStack(): ItemStack

    /**
     * Determines whether or not an upgrade can safely be removed from an UpgradeContainer without losing data.
     */
    fun canSafelyBeRemovedFrom(context: UpgradeContainer): Boolean = false
}
