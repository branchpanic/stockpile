package me.branchpanic.mods.stockpile.api.upgrade

/**
 * An UpgradeApplier can accept upgrades to its functionality.
 *
 * UpgradeApplier BlockEntities need not implement "right-click to upgrade" functionality themselves. Stockpile
 * automatically handles this for [UpgradeItem]s, calling applyUpgrade when necessary conditions are met.
 */
interface UpgradeApplier {

    /**
     * Upgrades that are currently applied.
     */
    val appliedUpgrades: List<Upgrade>

    /**
     * The maximum number of upgrades that can be applied.
     */
    val maxUpgrades: Int

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