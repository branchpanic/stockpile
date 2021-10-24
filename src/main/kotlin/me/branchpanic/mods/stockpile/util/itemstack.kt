package me.branchpanic.mods.stockpile.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

@Deprecated("Use ItemVariant")
fun ItemStack.canStackWith(other: ItemStack): Boolean = isEmpty ||
        other.isEmpty ||
        (ItemStack.areItemsEqual(withCount(1), other.withCount(1)) && ItemStack.areNbtEqual(
            this,
            other
        ))

@Deprecated("Use ItemVariant")
fun ItemStack.withCount(count: Int): ItemStack {
    val newStack = copy()
    newStack.count = count
    return newStack
}

@Deprecated("Use ItemVariant")
fun ItemStack.giveTo(player: PlayerEntity) {
    player.inventory.offerOrDrop(this)
}
