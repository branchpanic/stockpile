package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import me.branchpanic.mods.stockpile.extension.canStackWith
import me.branchpanic.mods.stockpile.extension.withCount
import net.minecraft.item.ItemStack

class ItemStackQuantizer(override val reference: ItemStack, override val amount: Long) : Quantizer<ItemStack> {
    override fun withAmount(amount: Long): Quantizer<ItemStack> {
        return ItemStackQuantizer(reference, amount)
    }

    override fun canMergeWith(other: Quantizer<ItemStack>): Boolean {
        return reference.canStackWith(other.reference)
    }

    override fun toObjects(): List<ItemStack> {
        val fullStacks = amount / reference.maxCount
        val remainderStackCount = amount % reference.maxCount

        return generateSequence { reference.withCount(reference.maxCount) }.take(fullStacks.toInt()).toList() +
                listOf(reference.withCount(remainderStackCount.toInt()))
    }
}

fun ItemStack.toQuantizer(): Quantizer<ItemStack> = ItemStackQuantizer(this.withCount(1), count.toLong())
