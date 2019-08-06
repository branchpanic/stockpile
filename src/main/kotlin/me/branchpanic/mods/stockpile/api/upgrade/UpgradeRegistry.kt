package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager

/**
 * The UpgradeRegistry holds a map of Identifiers to UpgradeTypes, and is used for persisting Upgrades.
 */
object UpgradeRegistry {
    private val LOGGER = LogManager.getLogger("stockpile")
    private val MARKER = MarkerManager.getMarker("UPGRADES")

    private const val ID_TAG = "ID"
    private const val DATA_TAG = "Data"

    val UPGRADE_HEADER_STYLE: Style = Style().setColor(Formatting.GREEN)
    val UPGRADE_TOOLTIP_STYLE: Style = Style().setColor(Formatting.GRAY)

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
    fun readUpgrade(tag: CompoundTag): Upgrade? = upgrades[Identifier(
        tag.getString(
            ID_TAG
        )
    )]?.reader?.invoke(
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
            LOGGER.warn(MARKER, "Failed to write upgrade data for ${upgrade.id}!")
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
    fun createTooltip(container: UpgradeContainer): List<Text> {
        val upgrades = container.appliedUpgrades

        if (upgrades.isEmpty()) {
            return emptyList()
        }

        return listOf(
            TranslatableText(
                "ui.stockpile.upgrades",
                container.appliedUpgrades.size,
                container.maxUpgrades
            ).setStyle(UPGRADE_HEADER_STYLE)
        ) + upgrades.map { u -> u.description.setStyle(UPGRADE_TOOLTIP_STYLE) }
    }
}