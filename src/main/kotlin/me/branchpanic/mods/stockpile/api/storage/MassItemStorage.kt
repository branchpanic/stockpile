package me.branchpanic.mods.stockpile.api.storage

import me.branchpanic.mods.stockpile.api.itemEquals
import me.branchpanic.mods.stockpile.api.withAmount
import net.minecraft.item.ItemStack

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

    override fun add(amount: Long): Long {
        val newAmount = storedItems + amount

        if (newAmount > capacity) {
            storedItems = capacity
            return newAmount - capacity
        }

        storedItems = newAmount
        return 0
    }

    override fun remove(amount: Long): Long {
        return if (amount >= storedItems) {
            val amountRemoved = storedItems
            storedItems = 0

            if (clearWhenEmpty) {
                clear()
            }

            amountRemoved
        } else {
            storedItems -= amount
            amount
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

        val resultingStack = storedStack.copy()
        val removableAmount = remove(amount)

        val removableFullStacks = removableAmount / resultingStack.maxAmount
        val removableRemainingStack = removableAmount - (resultingStack.maxAmount * removableFullStacks)

        val fullStacks = (0 until removableFullStacks).map { resultingStack.withAmount(resultingStack.maxAmount) }
        val remainderStack = resultingStack.copy().withAmount(removableRemainingStack.toInt())

        return (fullStacks + remainderStack).filterNot { s -> s.isEmpty }
    }
}