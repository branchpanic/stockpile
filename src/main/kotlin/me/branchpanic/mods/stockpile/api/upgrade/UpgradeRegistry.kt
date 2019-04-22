package me.branchpanic.mods.stockpile.api.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Style
import net.minecraft.text.TextComponent
import net.minecraft.text.TextFormat
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.Identifier

/**
 * The UpgradeRegistry holds a map of Identifiers to UpgradeTypes, and is used for persisting Upgrades.
 */
object UpgradeRegistry {
    private const val ID_TAG = "ID"
    private const val DATA_TAG = "Data"

    val UPGRADE_HEADER_STYLE: Style = Style().setColor(TextFormat.GREEN)
    val UPGRADE_TOOLTIP_STYLE: Style = Style().setColor(TextFormat.GRAY)

    private var upgrades = emptyMap<Identifier, UpgradeType>()

    /**
     * Registers the given UpgradeType to the UpgradeRegistry.
     */
    fun register(id: Identifier, type: UpgradeType) {
        upgrades = upgrades + (id to type)
    }

    /**
     * Reads a registered Upgrade from the given CompoundTag or returns null if it does not exist.
     */
    fun readUpgrade(tag: CompoundTag): Upgrade? = upgrades[Identifier(tag.getString(ID_TAG))]?.reader?.invoke(
        tag.getCompound(DATA_TAG)
    )

    /**
     * Writes the given registered Upgrade to a new CompoundTag.
     */
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

    /**
     * Creates a tooltip of TextComponents describing all the upgrades present on the given UpgradeApplier.
     */
    fun createTooltip(applier: UpgradeApplier): List<TextComponent> {
        val upgrades = applier.appliedUpgrades

        if (upgrades.isEmpty()) {
            return emptyList()
        }

        return listOf(
            TranslatableTextComponent(
                "ui.stockpile.upgrades",
                applier.appliedUpgrades.size,
                applier.maxUpgrades
            ).setStyle(UPGRADE_HEADER_STYLE)
        ) + upgrades.map { u -> u.description.setStyle(UPGRADE_TOOLTIP_STYLE) }
    }
}