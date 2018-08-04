package notjoe.stockpile.util

import net.minecraft.item.ItemStack

fun ItemStack.withCount(newCount: Int): ItemStack {
    val copiedStack = copy()
    copiedStack.count = newCount
    return copiedStack
}

// An adaptation of ItemStack::areItemStacksEqual which doesn't factor in quantity.
fun ItemStack.isStackableWith(other: ItemStack): Boolean {
    return if (item !== other.item) {
        false
    } else if (tagCompound == null && other.tagCompound != null) {
        false
    } else {
        tagCompound == null || this.tagCompound == other.tagCompound
    }
}