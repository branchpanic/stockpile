package me.branchpanic.mods.stockpile.content.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.Identifier
import kotlin.math.max

class MultiplierUpgrade(private val factor: Int) : ItemBarrelUpgrade {
    companion object {
        const val FACTOR_KEY = "Factor"
        val TYPE = UpgradeType({ t -> fromTag(t) }, { u -> (u as MultiplierUpgrade).toTag() })

        fun fromTag(tag: CompoundTag): MultiplierUpgrade = MultiplierUpgrade(max(1, tag.getInt(FACTOR_KEY)))
    }

    override val id: Identifier = Stockpile.id("multiplier")
    override val description: Component = TranslatableComponent("upgrade.stockpile.multiplier", factor)

    override fun upgradeMaxStacks(currentMaxStacks: Int): Int = currentMaxStacks * factor

    fun toTag(): CompoundTag {
        return CompoundTag().apply {
            putInt(FACTOR_KEY, factor)
        }
    }
}