package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Identifier

/**
 * An Upgrade changes the functionality of an UpgradeApplier in some way. This interface is rather useless on its own
 * and should be implemented in conjunction with target-specific interfaces such as
 * [me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade].
 *
 * Upgrades must be registered with the [UpgradeRegistry], otherwise they will not persist properly and potentially
 * leave the game in an unstable state.
 */
interface Upgrade {

    /**
     * The Identifier tying this Upgrade to an [UpgradeType].
     */
    val id: Identifier

    val name: Component
        get() = TranslatableComponent("upgrade.${id.namespace}.${id.path}.name")

    /**
     * The description of this Upgrade, often used in tooltips.
     */
    val description: Component

    /**
     * Determines which upgrades in a given list conflict with this Upgrade.
     */
    fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> {
        return emptyList()
    }
}