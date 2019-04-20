package me.branchpanic.mods.stockpile.api.upgrade

/**
 * An UpgradeApplier can accept upgrades to its functionality.
 */
interface UpgradeApplier {

    /**
     * Upgrades that are currently applied.
     */
    val appliedUpgrades: List<Upgrade>

    /**
     * Determines whether or not the given upgrade can be applied to this UpgradeApplier. This generally means checking
     * that the given Upgrade implements a certain interface.
     */
    fun canApplyUpgrade(u: Upgrade): Boolean

    /**
     * Applies the given upgrade to this UpgradeApplier. Eligibility to apply must be checked with [canApplyUpgrade]
     * and [Upgrade.getConflictingUpgrades] beforehand.
     */
    fun applyUpgrade(u: Upgrade)
}