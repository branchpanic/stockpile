package me.branchpanic.mods.stockpile.api.storage

import me.branchpanic.mods.stockpile.api.itemEquals
import me.branchpanic.mods.stockpile.api.withAmount
import net.minecraft.item.ItemStack

class MassItemStackStorage(
    private val maxStacks: Int,
    private var storedItems: Long = 0L,
    private var storedStack: ItemStack = ItemStack.EMPTY,
    var clearInstanceWhenEmpty: Boolean = true
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
        clearInstanceWhenEmpty = true

        if (isEmpty) {
            clear()
        }
    }

    override fun retainInstanceWhenEmpty() {
        clearInstanceWhenEmpty = false
    }

    override fun accepts(t: ItemStack): Boolean = storedStack.isEmpty || t.itemEquals(storedStack)

    override fun add(amount: Long): Long {
        val newAmount = storedItems + amount

        if (newAmount > capacity) {
            storedItems = capacity
            return newAmount - capacity
        }

        storedItems = newAmount
        return 0
    }

    override fun remove(amount: Long) {
        if (amount >= storedItems) {
            storedItems = 0

            if (clearInstanceWhenEmpty) {
                clear()
            }
        } else {
            storedItems -= amount
        }
    }

    override fun offer(ts: List<ItemStack>): List<ItemStack> {
        if (ts.isEmpty()) {
            return emptyList()
        }

        if (!instanceIsSet) {
            storedStack = ts.first().withAmount(1)
        }

        return ts.map { s ->
            if (accepts(s)) {
                s.copy().withAmount(add(s.amount.toLong()).toInt())
            } else {
                s
            }
        }.filterNot { s -> s.isEmpty }
    }

    override fun take(amount: Long): List<ItemStack> {
        if (amount == 0L || storedStack.isEmpty || amountStored == 0L) {
            return emptyList()
        }

        val removableFullStacks = amount / storedStack.maxAmount
        val removableRemainingStack = (amount % storedStack.maxAmount).toInt()

        remove(amount)

        val fullStacks = (0 until removableFullStacks).map { storedStack.copy().withAmount(storedStack.maxAmount) }
        val remainderStack = storedStack.copy().withAmount(removableRemainingStack)

        return (fullStacks + remainderStack).filterNot { s -> s.isEmpty }
    }
}