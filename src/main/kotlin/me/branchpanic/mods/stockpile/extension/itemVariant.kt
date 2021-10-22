@file:Suppress("DEPRECATION", "UnstableApiUsage")

package me.branchpanic.mods.stockpile.extension

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.item.ItemStack

fun ItemVariant.unpack(amount: Long) = sequence<ItemStack> {
    assert(amount >= 0)

    val itemVariant = this@unpack
    val referenceStack = itemVariant.toStack()
    val maxCount = referenceStack.maxCount

    val fullStacks = amount / maxCount
    val remainderStack = amount % maxCount

    for (i in 1..fullStacks) yield(itemVariant.toStack(maxCount))
    if (remainderStack > 0) yield(itemVariant.toStack(remainderStack.toInt()))
}