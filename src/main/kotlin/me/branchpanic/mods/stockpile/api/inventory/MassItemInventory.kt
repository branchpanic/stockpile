package me.branchpanic.mods.stockpile.api.inventory

import me.branchpanic.mods.stockpile.api.storage.MassStorage
import me.branchpanic.mods.stockpile.api.withAmount
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import kotlin.math.min

/**
 * A MassItemInventory wraps a MassStorage of ItemStacks into a SidedInventory.
 */
class MassItemInventory(
    var storage: MassStorage<ItemStack>,
    val onChanged: () -> Unit
) : SidedInventory {

    override fun markDirty() {
    }

    companion object {
        const val INPUT_SLOT = 0
        const val OUTPUT_SLOT = 1
    }

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean {
        return slot == INPUT_SLOT && stack != null && storage.accepts(stack)
    }

    override fun getInvStack(slot: Int): ItemStack {
        if (storage.isEmpty || !storage.instanceIsSet) {
            return ItemStack.EMPTY
        }

        return when (slot) {
            INPUT_SLOT -> {
                val amount = storage.amountStored
                val capacity = storage.capacity
                val maxStackAmount = storage.currentInstance.maxAmount

                if (amount < maxStackAmount) {
                    return ItemStack.EMPTY
                }

                val overflowThreshold = capacity - maxStackAmount

                return if (storage.amountStored < overflowThreshold) {
                    ItemStack.EMPTY
                } else {
                    storage.currentInstance.withAmount((amount - overflowThreshold).toInt())
                }
            }

            OUTPUT_SLOT -> storage.currentInstance.withAmount(min(64, storage.amountStored).toInt())

            else -> ItemStack.EMPTY
        }
    }

    override fun clear() {
        storage.remove(storage.amountStored)
        onChanged()
    }

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        if (stack == null || !isValidInvStack(slot, stack)) {
            return
        }

        storage.offer(stack)
        onChanged()
    }

    override fun removeInvStack(slot: Int): ItemStack {
        if (slot != OUTPUT_SLOT) return ItemStack.EMPTY

        val result = storage.take(storage.currentInstance.maxAmount.toLong()).getOrElse(0) { ItemStack.EMPTY }
        onChanged()

        return result
    }

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean = true

    override fun getInvSize(): Int = 2

    override fun takeInvStack(slot: Int, amount: Int): ItemStack {
        val result = storage.take(amount.toLong()).getOrElse(0) { ItemStack.EMPTY }
        onChanged()

        return result
    }

    override fun isInvEmpty(): Boolean = storage.isEmpty

    override fun getInvAvailableSlots(side: Direction?): IntArray {
        return intArrayOf(INPUT_SLOT, OUTPUT_SLOT)
    }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, side: Direction?): Boolean {
        return slot == OUTPUT_SLOT && stack != null && !storage.isEmpty && storage.accepts(stack)
    }

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, side: Direction?): Boolean {
        return slot == INPUT_SLOT && stack != null && storage.accepts(stack)
    }
}