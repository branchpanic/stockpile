package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.MassStorage
import me.branchpanic.mods.stockpile.itemEquals
import me.branchpanic.mods.stockpile.withAmount
import net.minecraft.item.ItemStack

/**
 * A MassItemStorage is an implementation of a [MassStorage] for storing ItemStacks.
 */
class MassItemStorage(
    val maxStacks: Int,
    private var storedItems: Long = 0L,
    var storedStack: ItemStack = ItemStack.EMPTY,
    var clearWhenEmpty: Boolean = true
) : MassStorage<ItemStack> {
    override val amountStored: Long
        get() = storedItems

    override val capacity: Long
        get() = (currentInstance.maxAmount * maxStacks).toLong()

    override val currentInstance: ItemStack
        get() = storedStack

    override val instanceIsSet: Boolean
        get() = !currentInstance.isEmpty

    private fun clear() {
        storedItems = 0L
        storedStack = ItemStack.EMPTY
    }

    override fun clearInstanceWhenEmpty() {
        clearWhenEmpty = true

        if (isEmpty) {
            clear()
        }
    }

    override fun retainInstanceWhenEmpty() {
        clearWhenEmpty = false
    }

    override fun accepts(t: ItemStack): Boolean = storedStack.isEmpty || t.itemEquals(storedStack)

    override fun add(amount: Long, simulate: Boolean): Long {
        val newAmount = storedItems + amount

        if (newAmount > capacity) {
            if (!simulate) {
                storedItems = capacity
            }

            return newAmount - capacity
        }

        if (!simulate) {
            storedItems = newAmount
        }

        return 0
    }

    override fun remove(amount: Long, simulate: Boolean): Long = if (amount >= storedItems) {
        val amountRemoved = storedItems

        if (!simulate) {
            storedItems = 0

            if (clearWhenEmpty) {
                clear()
            }
        }

        amountRemoved
    } else {
        if (!simulate) {
            storedItems -= amount
        }
        amount
    }

    override fun offer(ts: List<ItemStack>, simulate: Boolean): List<ItemStack> {
        return ts.map { t -> offer(t, simulate) }.filterNot { t -> t.isEmpty }
    }

    override fun offer(t: ItemStack, simulate: Boolean): ItemStack {
        if (t.isEmpty || !accepts(t)) return t

        if (!simulate && !instanceIsSet) {
            storedStack = t.withAmount(1)
        }

        val remainder = add(t.amount.toLong(), simulate).toInt()

        return t.withAmount(remainder)
    }

    override fun take(amount: Long, simulate: Boolean): List<ItemStack> {
        if (amount == 0L || storedStack.isEmpty || amountStored == 0L) {
            return emptyList()
        }

        val removableAmount = remove(amount, simulate)

        val removableFullStacks = removableAmount / storedStack.maxAmount
        val removableRemainingStack = removableAmount - (storedStack.maxAmount * removableFullStacks)

        val fullStacks = (0 until removableFullStacks).map { storedStack.withAmount(storedStack.maxAmount) }
        val remainderStack = storedStack.withAmount(removableRemainingStack.toInt())

        return (fullStacks + remainderStack).filterNot { s -> s.isEmpty }
    }
}