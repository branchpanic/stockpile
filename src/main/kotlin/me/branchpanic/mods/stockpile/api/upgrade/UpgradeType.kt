package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

typealias UpgradeReader = (CompoundTag) -> Upgrade
typealias UpgradeWriter = (Upgrade) -> CompoundTag
typealias ItemFactory = (Upgrade) -> ItemStack

/**
 * An UpgradeType is used to serialize and deserialize a family of Upgrades (registered in the UpgradeRegistry) to and
 * from CompoundTags.
 */
class UpgradeType(val reader: UpgradeReader, val writer: UpgradeWriter)