package me.branchpanic.mods.stockpile.extension

import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

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

fun ItemStack.giveTo(player: PlayerEntity, playSound: Boolean = true) {
    if (playSound) {
        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.1f, 0.7f)
    }

    player.inventory.insertStack(this)

    if (isEmpty) {
        return
    }

    player.world.spawnEntity(ItemEntity(player.world, player.x, player.y, player.z, this))
}
