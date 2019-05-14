package me.branchpanic.mods.stockpile.content.upgrade

import com.google.common.base.MoreObjects
import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent

class CapacityUpgrade(private val amount: Int) : ItemBarrelUpgrade {
    companion object {
        private const val AMOUNT_KEY = "Amount"

        val TYPE = UpgradeType({ t -> fromTag(t) }, { u -> (u as CapacityUpgrade).toTag() })

        private fun fromTag(tag: CompoundTag): CapacityUpgrade = CapacityUpgrade(tag.getInt(AMOUNT_KEY))
    }

    override val id = Stockpile.id("capacity")
    override val description: Component = TranslatableComponent("upgrade.stockpile.capacity", amount)

    override fun upgradeMaxStacks(currentMaxStacks: Int): Int = currentMaxStacks + amount

    fun toTag(): CompoundTag {
        return CompoundTag().apply {
            putInt(AMOUNT_KEY, amount)
        }
    }

    override fun toString(): String = MoreObjects.toStringHelper(this).add("amount", amount).toString()
}
