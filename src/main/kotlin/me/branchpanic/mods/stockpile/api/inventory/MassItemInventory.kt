package me.branchpanic.mods.stockpile.api.inventory

import me.branchpanic.mods.stockpile.api.storage.MassItemStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class MassItemInventory(val storage: MassItemStorage) : Inventory {
    companion object {
        const val INPUT_SLOT = 0
        const val OUTPUT_SLOT = 1
    }

    override fun isValidInvStack(int_1: Int, itemStack_1: ItemStack?): Boolean =
        int_1 == INPUT_SLOT && itemStack_1 != null && storage.accepts(itemStack_1)

    override fun getInvStack(p0: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun markDirty() {

    }

    override fun clear() {
        storage.remove(storage.amountStored)
    }

    override fun setInvStack(p0: Int, p1: ItemStack?) {
        if (p0 != INPUT_SLOT || p1 == null || !storage.accepts(p1)) {
            return
        }

        storage.add(p1.amount.toLong())
    }

    override fun removeInvStack(p0: Int): ItemStack {
        if (p0 != OUTPUT_SLOT) return ItemStack.EMPTY

        return storage.take(storage.currentInstance.maxAmount.toLong())[0]
    }

    override fun canPlayerUseInv(p0: PlayerEntity?): Boolean = true

    override fun getInvSize(): Int = 2

    override fun takeInvStack(p0: Int, p1: Int): ItemStack = storage.take(p1.toLong())[0]

    override fun isInvEmpty(): Boolean = storage.isEmpty
}