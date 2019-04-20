package me.branchpanic.mods.stockpile.api.upgrade

import net.minecraft.nbt.CompoundTag

typealias UpgradeReader = (CompoundTag) -> Upgrade
typealias UpgradeWriter = (Upgrade) -> CompoundTag

class UpgradeDefinition(val reader: UpgradeReader, val writer: UpgradeWriter)