@file:Suppress("DEPRECATION", "UnstableApiUsage")

package me.branchpanic.mods.stockpile.blockentity

import me.branchpanic.mods.stockpile.api.FuzzyTransactionAmount
import me.branchpanic.mods.stockpile.api.StorageDeviceBlockEntity
import me.branchpanic.mods.stockpile.block.TestBarrelBlock
import me.branchpanic.mods.stockpile.extension.unpack
import me.branchpanic.mods.stockpile.impl.ItemStorageDeviceStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos

class ItemStorageDeviceBlockEntity(
    blockPos: BlockPos?,
    blockState: BlockState?,
) :
    StorageDeviceBlockEntity(TYPE, blockPos, blockState) {
    companion object {
        val TYPE: BlockEntityType<ItemStorageDeviceBlockEntity> =
            BlockEntityType.Builder
                .create(::ItemStorageDeviceBlockEntity, TestBarrelBlock)
                .build(null)
    }

    var storage: ItemStorageDeviceStorage = ItemStorageDeviceStorage()
        private set

    init {
        Transaction.openOuter().use { tx ->
            storage.insert(ItemVariant.of(Items.APPLE), 100, tx)
            tx.commit()
        }
    }

    override var locked: Boolean
        get() = storage.locked
        set(value) { storage.locked = value }

    override fun markDirty() {
        super.markDirty()

        if (!storage.locked)

        world?.apply {
            updateListeners(pos, getBlockState(pos), getBlockState(pos), 3)
        }
    }

    override fun giveToPlayer(player: PlayerEntity, amount: FuzzyTransactionAmount): Long {
        if (storage.isResourceBlank || storage.amount <= 0L) return 0L

        val givenResource = storage.resource
        val givenAmount: Long

        Transaction.openOuter().use { tx ->
            givenAmount = when (amount) {
                FuzzyTransactionAmount.ONE -> {
                    storage.extract(storage.resource, 1L, tx)
                }
                FuzzyTransactionAmount.MANY -> {
                    storage.extract(storage.resource, storage.resource.toStack().maxCount.toLong(), tx)
                }
                FuzzyTransactionAmount.ALL -> {
                    TODO()
                }
            }

            tx.commit()
        }

        markDirty()
        givenResource.unpack(givenAmount).forEach(player::giveItemStack)
        return givenAmount
    }

    override fun takeFromPlayer(player: PlayerEntity, amount: FuzzyTransactionAmount): Long {
        if (storage.amount >= storage.capacity) return 0L

        var takenAmount = 0L

        Transaction.openOuter().use { tx ->
            when (amount) {
                FuzzyTransactionAmount.ONE -> TODO()
                FuzzyTransactionAmount.MANY -> {
                    val mainHandStack = player.mainHandStack
                    val takenResource = storage.resource
                    if (takenResource.isBlank || takenResource.matches(mainHandStack)) {
                        val inserted = storage.insert(ItemVariant.of(mainHandStack), mainHandStack.count.toLong(), tx)
                        val remaining = mainHandStack.count - inserted.toInt()

                        takenAmount = inserted
                        player.setStackInHand(Hand.MAIN_HAND, takenResource.toStack(remaining))
                    }
                }
                FuzzyTransactionAmount.ALL -> {
                    for (i in player.inventory.main.indices) {
                        val itemStack = player.inventory.main[i]
                        if (itemStack.isEmpty) continue

                        val takenResource = storage.resource
                        if (!takenResource.isBlank && !takenResource.matches(itemStack)) continue

                        val inserted = storage.insert(ItemVariant.of(itemStack), itemStack.count.toLong(), tx)
                        println("post-insert: ${storage.resource}")
                        takenAmount += inserted

                        val remaining = itemStack.count - inserted.toInt()
                        player.inventory.main[i] = takenResource.toStack(remaining)
                    }
                }
            }

            tx.commit()
            player.inventory.markDirty()
        }

        return takenAmount
    }

    override fun writeNbt(nbtCompound: NbtCompound?): NbtCompound {
        return super.writeNbt(nbtCompound).apply {
            put("storage", storage.toNbt())
        }
    }

    override fun readNbt(nbtCompound: NbtCompound?) {
        super.readNbt(nbtCompound)

        if (nbtCompound == null) return
        if ("storage" !in nbtCompound) return
        storage.readNbt(nbtCompound.getCompound("storage"))
    }
}