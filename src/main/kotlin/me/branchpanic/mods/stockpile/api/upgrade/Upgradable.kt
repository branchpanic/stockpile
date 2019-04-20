package me.branchpanic.mods.stockpile.api.upgrade

interface Upgradable {
    fun canAcceptUpgrade(u: Upgrade): Boolean
    fun applyUpgrade(u: Upgrade)
}