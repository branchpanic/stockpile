package notjoe.stockpile.util.ext

import net.minecraft.item.ItemStack

/**
 * Creates a copy of an ItemStack with a given count.
 */
fun ItemStack.withCount(newCount: Int): ItemStack {
    val copiedStack = copy()
    copiedStack.count = newCount
    return copiedStack
}

/**
 * Checks if this ItemStack is stackable with another given ItemStack (same item and NBT data).
 */
fun ItemStack.isStackableWith(other: ItemStack): Boolean {
    return if (item !== other.item) {
        false
    } else if (tagCompound == null && other.tagCompound != null) {
        false
    } else {
        tagCompound == null || this.tagCompound == other.tagCompound
    }
}