package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener
import alexiil.mc.lib.attributes.item.ItemTransferable
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import me.branchpanic.mods.stockpile.impl.storage.MassItemStorage
import me.branchpanic.mods.stockpile.itemEquals
import net.minecraft.item.ItemStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A FixedMassItemInv wraps a MassItemStorage into a FixedItemInv.
 */
class FixedMassItemInv(internal var storage: MassItemStorage, var voidExtraItems: Boolean = false) : FixedItemInv,
    ItemTransferable {
    companion object {
        const val INBOUND_SLOT = 0
        const val OUTBOUND_SLOT = 1
    }

    private var listeners: List<ItemInvSlotChangeListener> = emptyList()

    private fun fireListeners(slot: Int, before: ItemStack, after: ItemStack) =
        listeners.forEach { l -> l.onChange(this, slot, before, after) }

    override fun getInvStack(slot: Int): ItemStack =
        when (slot) {
            INBOUND_SLOT -> ItemStack.EMPTY
            OUTBOUND_SLOT -> storage.take(
                storage.currentInstance.maxAmount.toLong(),
                simulate = true
            ).getOrElse(0) { ItemStack.EMPTY }
            else -> throw IllegalArgumentException("slot index out of bounds")
        }

    override fun setInvStack(slot: Int, stack: ItemStack?, simulation: Simulation?): Boolean {
        if (stack == null || !isItemValidForSlot(slot, stack)) {
            return false
        }

        if (storage.isEmpty) {
            return storage.offer(stack).isEmpty
        }

        if (storage.isFull && voidExtraItems) {
            return true
        }

        val before = getInvStack(slot)
        val change = stack.amount - before.amount

        when {
            change > 0 -> {
                val remainder = storage.add(change.toLong())
                if (remainder != 0L) {
                    throw RuntimeException("lost $remainder items in a transaction")
                }
            }
            change < 0 -> storage.remove(abs(change).toLong())
        }

        fireListeners(slot, before, getInvStack(slot))
        return true
    }

    override fun isItemValidForSlot(slot: Int, stack: ItemStack?): Boolean {
        if (stack == null) {
            return false
        }

        if (slot == INBOUND_SLOT && storage.isFull && !voidExtraItems) {
            return false
        }

        return storage.accepts(stack) || stack.isEmpty
    }

    override fun addListener(listener: ItemInvSlotChangeListener?, removalToken: ListenerRemovalToken?): ListenerToken {
        if (listener == null || removalToken == null) {
            throw NullPointerException("null parameter when trying to add a listener")
        }

        listeners = listeners + listener

        return ListenerToken {
            listeners = listeners.filterNot { l -> l == listener }
            removalToken.onListenerRemoved()
        }
    }

    override fun getSlotCount(): Int = 2

    override fun getMaxAmount(slot: Int, stack: ItemStack?): Int {
        if (stack == null || !stack.itemEquals(storage.storedStack)) {
            return 0
        }

        if (voidExtraItems && slot == INBOUND_SLOT) {
            return storage.storedStack.maxAmount
        }

        return when (slot) {
            INBOUND_SLOT -> max(
                0,
                min(storage.storedStack.amount.toLong(), storage.capacity - storage.amountStored)
            ).toInt()
            OUTBOUND_SLOT -> min(storage.storedStack.amount.toLong(), storage.amountStored).toInt()
            else -> throw IllegalArgumentException("slot index out of bounds")
        }
    }

    override fun getTransferable(): ItemTransferable = this

    override fun attemptInsertion(stack: ItemStack?, simulation: Simulation?): ItemStack {
        if (stack == null) return ItemStack.EMPTY

        fireListeners(-1, ItemStack.EMPTY, ItemStack.EMPTY)
        return storage.offer(stack, simulation == Simulation.SIMULATE)
    }

    override fun attemptExtraction(filter: ItemFilter?, amount: Int, simulation: Simulation?): ItemStack {
        if (filter == null || !filter.matches(storage.storedStack)) return ItemStack.EMPTY

        fireListeners(-1, ItemStack.EMPTY, ItemStack.EMPTY)
        return storage.take(amount.toLong(), simulation == Simulation.SIMULATE).getOrElse(0) { ItemStack.EMPTY }
    }
}