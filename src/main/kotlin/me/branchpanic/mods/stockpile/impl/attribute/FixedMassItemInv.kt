package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener
import alexiil.mc.lib.attributes.item.ItemTransferable
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import me.branchpanic.mods.stockpile.extension.withCount
import me.branchpanic.mods.stockpile.impl.storage.firstStack
import me.branchpanic.mods.stockpile.impl.storage.oneStackToQuantizer
import me.branchpanic.mods.stockpile.impl.storage.toQuantizer
import net.minecraft.item.ItemStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A FixedMassItemInv wraps a MutableMassStorage of ItemStacks into a FixedItemInv.
 */
class FixedMassItemInv(
    internal var storage: MutableMassStorage<ItemStack>,
    var voidExtraItems: Boolean = false
) :
    FixedItemInv,
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
            OUTBOUND_SLOT -> storage.removeAtMost(
                storage.contents.reference.oneStackToQuantizer(),
                simulate = true
            ).firstStack()
            else -> throw IllegalArgumentException("slot index out of bounds")
        }

    override fun setInvStack(slot: Int, stack: ItemStack?, simulation: Simulation?): Boolean {
        if (stack == null || !isItemValidForSlot(slot, stack)) {
            return false
        }

        if (storage.isEmpty) {
            return storage.addAtMost(stack.toQuantizer()).isEmpty
        }

        if (storage.isFull && voidExtraItems) {
            return true
        }

        val before = getInvStack(slot)
        val change = stack.count - before.count

        when {
            change > 0 -> {
                val remainder = storage.addAtMost(change.toLong())
                if (remainder != 0L) {
                    throw RuntimeException("lost $remainder items in a transaction")
                }
            }
            change < 0 -> storage.removeAtMost(abs(change).toLong())
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

        return storage.contents.canMergeWith(stack.toQuantizer())
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
        if (stack == null || !storage.contents.canMergeWith(stack.toQuantizer())) {
            return 0
        }

        if (voidExtraItems && slot == INBOUND_SLOT) {
            return storage.contents.reference.maxCount
        }

        return when (slot) {
            INBOUND_SLOT -> max(
                0,
                min(storage.contents.reference.maxCount.toLong(), storage.freeSpace)
            ).toInt()
            OUTBOUND_SLOT -> min(storage.contents.reference.maxCount.toLong(), storage.contents.amount).toInt()
            else -> throw IllegalArgumentException("slot index out of bounds")
        }
    }

    override fun getTransferable(): ItemTransferable = this

    override fun attemptInsertion(stack: ItemStack?, simulation: Simulation?): ItemStack {
        if (stack == null) return ItemStack.EMPTY

        fireListeners(-1, ItemStack.EMPTY, ItemStack.EMPTY)
        return storage.addAtMost(stack.toQuantizer(), simulation == Simulation.SIMULATE).firstStack()
    }

    override fun attemptExtraction(filter: ItemFilter?, amount: Int, simulation: Simulation?): ItemStack {
        if (filter == null || storage.contents.isEmpty || !filter.matches(storage.contents.reference)) return ItemStack.EMPTY

        fireListeners(-1, ItemStack.EMPTY, ItemStack.EMPTY)
        val extractedAmount = storage.removeAtMost(amount.toLong(), simulation == Simulation.SIMULATE)

        return if (extractedAmount <= 0) {
            ItemStack.EMPTY
        } else {
            // This is safe because the reference will only ever change on insertion.
            storage.contents.reference.withCount(extractedAmount.toInt())
        }
    }
}
