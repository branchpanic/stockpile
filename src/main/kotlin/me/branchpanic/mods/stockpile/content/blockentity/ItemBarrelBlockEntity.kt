package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.BarrelTransactionAmount
import me.branchpanic.mods.stockpile.impl.storage.ItemStackQuantizer
import me.branchpanic.mods.stockpile.impl.storage.MassItemStackStorage
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

class ItemBarrelBlockEntity : AbstractBarrelBlockEntity<ItemStack>(
    storage = MassItemStackStorage(ItemStackQuantizer.NONE, 0L),
    upgrades = TODO(),
    doubleClickThresholdMs = 5000,
    type = LegacyItemBarrelBlockEntity.TYPE
), BlockEntityClientSerializable {
    override fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        TODO("not implemented")
    }

    override fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        TODO("not implemented")
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        requireNotNull(tag)
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag?) {
        requireNotNull(tag)
        super.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        requireNotNull(tag)
        TODO()
    }

    override fun fromClientTag(tag: CompoundTag?) {
        requireNotNull(tag)
        TODO()
    }
}
