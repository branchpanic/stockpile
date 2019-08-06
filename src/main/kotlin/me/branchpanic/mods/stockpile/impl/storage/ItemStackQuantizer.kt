package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import me.branchpanic.mods.stockpile.extension.canStackWith
import me.branchpanic.mods.stockpile.extension.withCount
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ItemStackQuantizer(override val reference: ItemStack, override val amount: Long) : Quantizer<ItemStack> {
    companion object {
        val NONE = ItemStackQuantizer(reference = ItemStack.EMPTY, amount = 0L)
    }

    override fun withAmount(amount: Long): Quantizer<ItemStack> {
        return ItemStackQuantizer(reference, amount)
    }

    override fun canMergeWith(other: Quantizer<ItemStack>): Boolean {
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
        val otherQuantizer = other as? ItemStackQuantizer ?: return false
        return ItemStack.areItemsEqual(reference, other.reference) &&
                ItemStack.areTagsEqual(reference, other.reference) &&
                amount == otherQuantizer.amount
    }

    override fun hashCode(): Int {
        var result = reference.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun plus(other: Quantizer<ItemStack>): Quantizer<ItemStack> {
        val untypedResult = super.plus(other)

        // Note that:
        // n of EMPTY + m of something = (n + m) of something
        // However:
        // n of something - n of something = 0 of something
        return if (reference.isEmpty) {
            ItemStackQuantizer(other.reference, untypedResult.amount)
        } else {
            untypedResult
        }
    }
}

fun Quantizer<ItemStack>.firstStack(): ItemStack = toObjects().getOrElse(0) { ItemStack.EMPTY }

fun ItemStack.toQuantizer(amount: Long = count.toLong()): Quantizer<ItemStack> =
    ItemStackQuantizer(this.withCount(1), amount)

fun ItemStack.oneStackToQuantizer(): Quantizer<ItemStack> = toQuantizer(maxCount.toLong())

fun Item.toQuantizer(amount: Long): Quantizer<ItemStack> = ItemStackQuantizer(ItemStack(this, 1), amount)
