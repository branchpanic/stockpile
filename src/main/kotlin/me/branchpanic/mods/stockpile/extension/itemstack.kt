package me.branchpanic.mods.stockpile.extension

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

fun ItemStack.canStackWith(other: ItemStack): Boolean = isEmpty ||
        other.isEmpty ||
        (ItemStack.areItemsEqual(withCount(1), other.withCount(1)) && ItemStack.areTagsEqual(
            this,
            other
        ))

fun ItemStack.withCount(count: Int): ItemStack {
    val newStack = copy()
    newStack.count = count
    return newStack
}

fun ItemStack.giveTo(player: PlayerEntity) {
    player.inventory.offerOrDrop(player.world, this)
}
