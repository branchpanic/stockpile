package me.branchpanic.mods.stockpile.api.upgrade

import me.branchpanic.mods.stockpile.Stockpile
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier

object UpgradeRegistry {
    private const val ID_TAG = "ID"
    private const val DATA_TAG = "Data"

    private var upgrades = emptyMap<Identifier, UpgradeDefinition>()

    fun register(id: Identifier, definition: UpgradeDefinition) {
        upgrades = upgrades + (id to definition)
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
            Stockpile.LOGGER.warn("failed to write upgrade data for ${upgrade.id}. it will be removed!")
            return null
        }

        return CompoundTag().apply {
            putString(ID_TAG, upgrade.id.toString())
            put(DATA_TAG, upgradeData)
        }
    }
}