@file:Suppress("DEPRECATION", "UnstableApiUsage")

package me.branchpanic.mods.stockpile.impl

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.nbt.NbtCompound
import kotlin.math.max
import kotlin.math.min

class ItemStorageDeviceStorage :
    SnapshotParticipant<ResourceAmount<ItemVariant>>(),
    SingleSlotStorage<ItemVariant> {
    private var _amount: Long = 0L
    private var _capacity: Long = 1024L
    private var reference: ItemVariant = ItemVariant.blank()
    var locked: Boolean = true
        set(newLockState) {
            field = newLockState

            // If unlocked and empty, clear reference
            if (!newLockState && _amount <= 0L && !reference.isBlank) reference = ItemVariant.blank()
        }

    override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (resource == null || transaction == null) return 0L

        if (reference.isBlank) {
            println("setting reference to $resource")
            reference = resource
        } else if (!reference.matches(resource.toStack())) return 0L

        val insertable = maxAmount - max((_amount + maxAmount) - capacity, 0L)
        _amount += insertable

        return insertable
    }

    override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (resource == null || transaction == null) return 0L
        if (reference.isBlank) return 0L
        if (!reference.matches(resource.toStack())) return 0L

        val extractable = min(maxAmount, _amount)
        _amount -= extractable

        if (_amount == 0L && !locked) reference = ItemVariant.blank()

        return extractable
    }

    fun toNbt(): NbtCompound {
        return NbtCompound().apply {
            put("reference", reference.toNbt())
            putLong("amount", _amount)
            putLong("capacity", _capacity)
            putBoolean("locked", locked)
        }
    }

    fun readNbt(nbt: NbtCompound) {
        reference = ItemVariant.fromNbt(nbt.getCompound("reference"))
        _amount = nbt.getLong("amount")
        _capacity = nbt.getLong("capacity")
        locked = nbt.getBoolean("locked")
    }

    override fun isResourceBlank(): Boolean = reference.isBlank

    override fun getResource(): ItemVariant = reference

    override fun getAmount(): Long = _amount

    override fun getCapacity(): Long = _capacity

    override fun createSnapshot(): ResourceAmount<ItemVariant> = ResourceAmount(reference, _amount)

    override fun readSnapshot(snapshot: ResourceAmount<ItemVariant>?) {
        requireNotNull(snapshot)
        reference = snapshot.resource
        _amount = snapshot.amount
    }
}