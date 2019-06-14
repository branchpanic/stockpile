package me.branchpanic.mods.stockpile.content.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Identifier

class TrashUpgrade : ItemBarrelUpgrade {
    companion object {
        val TYPE = UpgradeType({ TrashUpgrade() }, { CompoundTag() })
    }

    override val id: Identifier = Stockpile.id("trash")
    override val description: Component = TranslatableComponent("upgrade.stockpile.trash")

    override fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> {
        return upgrades.filterIsInstance<TrashUpgrade>()
    }
}
