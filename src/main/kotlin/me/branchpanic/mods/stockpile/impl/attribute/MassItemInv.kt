package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener
import me.branchpanic.mods.stockpile.impl.storage.MassItemStorage
import me.branchpanic.mods.stockpile.withAmount
import net.minecraft.item.ItemStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MassItemInv(var storage: MassItemStorage, private var listeners: List<ItemInvSlotChangeListener> = emptyList()) :
    FixedItemInv {

    init {
        recalculateSlots()
    }

    companion object {
        const val INPUT_SLOT = 0
        const val OUTPUT_SLOT = 1
    }

    var inputStack: ItemStack = ItemStack.EMPTY
    var outputStack: ItemStack = ItemStack.EMPTY

    private fun handleChange(before: ItemStack, after: ItemStack) {
        println("===== Updating slots")
        if (storage.isEmpty) {
            storage.offer(after)
        } else {
            val change = after.amount - before.amount

            when {
                change > 0 -> storage.add(change.toLong())
                change < 0 -> storage.remove(abs(change.toLong()))
                else -> return
            }
        }

        recalculateSlots()
    }

    internal fun recalculateSlots() {
        if (storage.isEmpty) {
            inputStack = ItemStack.EMPTY
            outputStack = ItemStack.EMPTY
            return
        }

        val expectedOutput = min(storage.currentInstance.maxAmount.toLong(), storage.amountStored).toInt()

        val inputFillThreshold = storage.capacity - storage.currentInstance.maxAmount
        val expectedInput = max(0, storage.amountStored - inputFillThreshold).toInt()

        inputStack = storage.currentInstance.withAmount(expectedInput)
        outputStack = storage.currentInstance.withAmount(expectedOutput)
    }

    override fun isItemValidForSlot(slot: Int, item: ItemStack?): Boolean {
        return item != null && (storage.accepts(item) || item.isEmpty)
    }

    override fun getInvStack(slot: Int): ItemStack = when (slot) {
        INPUT_SLOT -> inputStack
        OUTPUT_SLOT -> outputStack
        else -> ItemStack.EMPTY
    }

    override fun setInvStack(slot: Int, stack: ItemStack?, simulation: Simulation?): Boolean {
        if (stack == null || !isItemValidForSlot(slot, stack)) {
            return false
        }

        if (simulation == Simulation.ACTION) {
            val oldInput = inputStack.copy()
            inputStack = stack
            handleChange(oldInput, inputStack.copy())
        }

        return true
    }

    override fun getSlotCount(): Int = 2

    override fun addListener(listener: ItemInvSlotChangeListener?, remover: ListenerRemovalToken?): ListenerToken {
        if (listener != null) {
            listeners += listener
        }

        return ListenerToken { remover?.onListenerRemoved() }
    }
}
