package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

/**
 * An Upgrade changes the functionality of an UpgradeApplier in some way. This interface is rather useless on its own
 * and should be implemented in conjunction with target-specific interfaces such as
 * [me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade].
 *
 * Upgrades must be registered with the [UpgradeRegistry], otherwise they will not persist properly and potentially
 * leave the game in an unstable state.
 *
 * TODO: All the casting in working with upgrades is totally what generics are for... I just don't have time to
 *       reimplement them properly.
 */
interface Upgrade {

    /**
     * The Identifier tying this Upgrade to an [UpgradeType].
     */
    val id: Identifier

    val name: Text
        get() = TranslatableText("upgrade.${id.namespace}.${id.path}.name")

    /**
     * The description of this Upgrade, often used in tooltips.
     */
    val description: Text

    /**
     * Determines which upgrades in a given list conflict with this Upgrade.
     */
    fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> = emptyList()

    fun getCorrespondingStack(): ItemStack = ItemStack.EMPTY

    /**
     * Determines whether or not an upgrade can safely be removed from an UpgradeApplier without losing data.
     */
    fun canSafelyBeRemovedFrom(context: UpgradeContainer): Boolean = false
}