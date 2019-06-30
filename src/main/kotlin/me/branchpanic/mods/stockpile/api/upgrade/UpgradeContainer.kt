package me.branchpanic.mods.stockpile.api.upgrade

/**
 * An UpgradeContainer can accept upgrades to its functionality.
 *
 * BlockEntities that implement UpgradeContainer will automatically have support for adding, viewing, and removing
 * upgrades via Stockpile.
 */
interface UpgradeContainer {

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
    fun isUpgradeTypeAllowed(u: Upgrade): Boolean

    /**
     * Applies the given upgrade to this UpgradeApplier. Eligibility to apply must be checked with
     * [isUpgradeTypeAllowed] and [Upgrade.getConflictingUpgrades] beforehand.
     */
    fun pushUpgrade(u: Upgrade)

    fun popUpgrade(): Upgrade
}