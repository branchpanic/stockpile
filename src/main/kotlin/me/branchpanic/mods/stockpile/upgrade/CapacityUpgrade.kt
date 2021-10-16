package me.branchpanic.mods.stockpile.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.Stockpile.id
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import me.branchpanic.mods.stockpile.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.impl.storage.MassItemStackStorage
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class CapacityUpgrade(private val amount: Int) : ItemBarrelUpgrade {
    companion object {
        private const val AMOUNT_KEY = "Amount"
        val TYPE = UpgradeType(
            reader = { t -> fromTag(t) },
            writer = { u -> (u as CapacityUpgrade).toTag() }
        )

        private fun fromTag(tag: NbtCompound): CapacityUpgrade = CapacityUpgrade(tag.getInt(AMOUNT_KEY))
    }

    override val id = id("capacity")
    override val description: Text = TranslatableText("upgrade.stockpile.capacity", amount)

    override fun upgradeMaxStacks(currentMaxStacks: Int): Int = currentMaxStacks + amount

    override fun canSafelyBeRemovedFrom(context: UpgradeContainer): Boolean {
        val barrel = context as? ItemBarrelBlockEntity ?: return false

        return barrel.storage.contents.amount <=
                ((barrel.storage as MassItemStackStorage).maxStacks - amount) *
                barrel.storage.contents.reference.maxCount
    }

    override fun toStack(): ItemStack = when (amount) {
        32 -> ItemStack(Stockpile.ITEMS[id("capacity_upgrade")])
        64 -> ItemStack(Stockpile.ITEMS[id("double_capacity_upgrade")])
        else -> ItemStack.EMPTY
    }

    fun toTag(): NbtCompound {
        return NbtCompound().apply {
            putInt(AMOUNT_KEY, amount)
        }
    }
}
