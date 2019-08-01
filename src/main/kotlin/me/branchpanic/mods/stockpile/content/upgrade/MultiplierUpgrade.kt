package me.branchpanic.mods.stockpile.content.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.Stockpile.id
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import me.branchpanic.mods.stockpile.content.blockentity.LegacyItemBarrelBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import kotlin.math.max

class MultiplierUpgrade(private val factor: Int) : ItemBarrelUpgrade {
    companion object {
        const val FACTOR_KEY = "Factor"
        val TYPE = UpgradeType({ t -> fromTag(t) }, { u -> (u as MultiplierUpgrade).toTag() })

        fun fromTag(tag: CompoundTag): MultiplierUpgrade = MultiplierUpgrade(max(1, tag.getInt(FACTOR_KEY)))
    }

    override val id: Identifier = id("multiplier")
    override val description: Text = TranslatableText("upgrade.stockpile.multiplier", factor)

    override fun upgradeMaxStacks(currentMaxStacks: Int): Int = currentMaxStacks * factor

    override fun canSafelyBeRemovedFrom(context: UpgradeContainer): Boolean {
        val barrel = context as? LegacyItemBarrelBlockEntity ?: return false

        return barrel.backingStorage.amountStored <=
                (barrel.backingStorage.maxStacks / factor) * barrel.backingStorage.storedStack.maxCount
    }

    override fun getCorrespondingStack(): ItemStack = when (factor) {
        2 -> ItemStack(Stockpile.ITEMS[id("multiplier_upgrade")])
        4 -> ItemStack(Stockpile.ITEMS[id("double_multiplier_upgrade")])
        else -> ItemStack.EMPTY
    }

    fun toTag(): CompoundTag {
        return CompoundTag().apply {
            putInt(FACTOR_KEY, factor)
        }
    }
}