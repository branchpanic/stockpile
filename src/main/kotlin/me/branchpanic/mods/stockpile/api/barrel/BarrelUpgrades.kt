package me.branchpanic.mods.stockpile.api.barrel

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

object BarrelUpgrades {
    private data class Entry(val reader: (CompoundTag) -> BarrelUpgrade, val writer: (BarrelUpgrade) -> CompoundTag)

    private const val ID_TAG = "ID"
    private var UPGRADES: Map<Identifier, Entry> = hashMapOf()

    fun register(id: Identifier, reader: (CompoundTag) -> BarrelUpgrade, writer: (BarrelUpgrade) -> CompoundTag) {
        UPGRADES = UPGRADES + (id to Entry(reader, writer))
    }

    fun toTag(upgrade: BarrelUpgrade): CompoundTag? {
        if (upgrade.id !in UPGRADES) {
            return null
        }

        val tag = CompoundTag()
        tag.putString(ID_TAG, upgrade.id.toString())
        tag.put("Data", UPGRADES.getValue(upgrade.id).writer(upgrade))
        return tag
    }

    fun fromTag(tag: CompoundTag): BarrelUpgrade? {
        val id: Identifier

        try {
            id = Identifier(tag.getString(ID_TAG))

            if (id !in UPGRADES) {
                return null
            }
        } catch (e: InvalidIdentifierException) {
            return null
        }

        return UPGRADES.getValue(id).reader(tag)
    }
}