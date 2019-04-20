package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.text.TextComponent
import net.minecraft.util.Identifier

interface Upgrade {
    val id: Identifier
    val description: TextComponent

    fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> {
        return emptyList()
    }
}