package me.branchpanic.mods.stockpile

import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

fun ItemStack.itemEquals(other: ItemStack): Boolean = ItemStack.areEqual(withAmount(1), other.withAmount(1))

fun ItemStack.withAmount(amount: Int): ItemStack {
    val newStack = copy()
    newStack.amount = amount
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
