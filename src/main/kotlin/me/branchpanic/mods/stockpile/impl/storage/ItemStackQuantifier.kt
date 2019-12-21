package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.Quantifier
import me.branchpanic.mods.stockpile.extension.canStackWith
import me.branchpanic.mods.stockpile.extension.withCount
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ItemStackQuantifier(override val reference: ItemStack, override val amount: Long) : Quantifier<ItemStack> {
    companion object {
        val NONE = ItemStackQuantifier(reference = ItemStack.EMPTY, amount = 0L)
    }

    override fun withAmount(amount: Long): Quantifier<ItemStack> {
        return ItemStackQuantifier(reference, amount)
    }

    override fun canMergeWith(other: Quantifier<ItemStack>): Boolean {
        return reference.canStackWith(other.reference)
    }

    override fun toObjects(): List<ItemStack> {
        if (reference.isEmpty || amount <= 0L) return emptyList()

        val fullStacks = amount / reference.maxCount
        val remainderStackCount = amount % reference.maxCount

        val fullStackList =
            generateSequence { reference.withCount(reference.maxCount) }.take(fullStacks.toInt()).toList()
        val remainderStackList = if (remainderStackCount > 0L) {
            listOf(reference.withCount(remainderStackCount.toInt()))
        } else emptyList()

        return fullStackList + remainderStackList
    }

    override fun toString(): String = "ItemStackQuantizer(reference = $reference, amount = $amount)"

    override fun equals(other: Any?): Boolean {
        val otherQuantizer = other as? ItemStackQuantifier ?: return false
        return ItemStack.areItemsEqual(reference, other.reference) &&
                ItemStack.areTagsEqual(reference, other.reference) &&
                amount == otherQuantizer.amount
    }

    override fun hashCode(): Int {
        var result = reference.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun plus(other: Quantifier<ItemStack>): Quantifier<ItemStack> {
        val untypedResult = super.plus(other)

        // Note that:
        // n of EMPTY + m of something = (n + m) of something
        // However:
        // n of something - n of something = 0 of something
        return if (reference.isEmpty) {
            ItemStackQuantifier(other.reference, untypedResult.amount)
        } else {
            untypedResult
        }
    }
}

fun Quantifier<ItemStack>.firstStack(): ItemStack = toObjects().getOrElse(0) { ItemStack.EMPTY }

fun ItemStack.toQuantizer(amount: Long = count.toLong()): Quantifier<ItemStack> =
    ItemStackQuantifier(this.withCount(1), amount)

fun ItemStack.oneStackToQuantizer(): Quantifier<ItemStack> = toQuantizer(maxCount.toLong())

fun Item.toQuantizer(amount: Long): Quantifier<ItemStack> = ItemStackQuantifier(ItemStack(this, 1), amount)
