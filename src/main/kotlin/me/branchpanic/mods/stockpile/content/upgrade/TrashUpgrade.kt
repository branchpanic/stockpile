package me.branchpanic.mods.stockpile.content.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.Stockpile.id
import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Identifier

class TrashUpgrade : ItemBarrelUpgrade {
    companion object {
        val TYPE = UpgradeType({ TrashUpgrade() }, { CompoundTag() })
    }

    override val id: Identifier = id("trash")
    override val description: Component = TranslatableComponent("upgrade.stockpile.trash")

    override fun getConflictingUpgrades(upgrades: List<Upgrade>): List<Upgrade> {
        return upgrades.filterIsInstance<TrashUpgrade>()
    }

    override fun getCorrespondingStack(): ItemStack {
        return ItemStack(Stockpile.ITEMS[id("trash_upgrade")])
    }

    override fun canSafelyBeRemovedFrom(context: UpgradeContainer): Boolean = true
}
