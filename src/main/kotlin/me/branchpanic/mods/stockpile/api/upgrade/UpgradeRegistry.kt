package me.branchpanic.mods.stockpile.api.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Style
import net.minecraft.text.TextComponent
import net.minecraft.text.TextFormat
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.Identifier

object UpgradeRegistry {
    private const val ID_TAG = "ID"
    private const val DATA_TAG = "Data"

    val UPGRADE_HEADER_STYLE = Style().setColor(TextFormat.GREEN)
    val UPGRADE_TOOLTIP_STYLE = Style().setColor(TextFormat.GRAY)

    private var upgrades = emptyMap<Identifier, UpgradeType>()

    fun register(id: Identifier, type: UpgradeType) {
        upgrades = upgrades + (id to type)
    }

    fun readUpgrade(tag: CompoundTag): Upgrade? = upgrades[Identifier(tag.getString(ID_TAG))]?.reader?.invoke(
        tag.getCompound(
            DATA_TAG
        )
    )

    fun writeUpgrade(upgrade: Upgrade): CompoundTag? {
        if (upgrade.id !in upgrades) {
            return null
        }

        val upgradeData = upgrades[upgrade.id]?.writer?.invoke(upgrade)

        if (upgradeData == null) {
            Stockpile.LOGGER.warn("failed to write upgrade data for ${upgrade.id}")
            return null
        }

        return CompoundTag().apply {
            putString(ID_TAG, upgrade.id.toString())
            put(DATA_TAG, upgradeData)
        }
    }

    fun createTooltip(upgrades: List<Upgrade>): List<TextComponent> {
        if (upgrades.isEmpty()) {
            return emptyList()
        }

        return listOf(TranslatableTextComponent("ui.stockpile.upgrades").setStyle(UPGRADE_HEADER_STYLE)) +
                upgrades.map { u -> u.description.setStyle(UPGRADE_TOOLTIP_STYLE) }
    }
}