package me.branchpanic.mods.stockpile.api

import net.minecraft.item.ItemStack

fun ItemStack.itemEquals(other: ItemStack): Boolean = ItemStack.areEqual(withAmount(1), other.withAmount(1))

fun ItemStack.withAmount(amount: Int): ItemStack {
    val newStack = copy()
    newStack.amount = amount
    return newStack
}