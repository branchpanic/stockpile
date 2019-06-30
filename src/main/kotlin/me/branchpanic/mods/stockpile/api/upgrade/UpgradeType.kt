package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.nbt.CompoundTag

typealias UpgradeReader = (CompoundTag) -> Upgrade
typealias UpgradeWriter = (Upgrade) -> CompoundTag

/**
 * An UpgradeType is used to serialize and deserialize a family of Upgrades (registered in the UpgradeRegistry) to and
 * from CompoundTags.
 */
class UpgradeType(val reader: UpgradeReader, val writer: UpgradeWriter)