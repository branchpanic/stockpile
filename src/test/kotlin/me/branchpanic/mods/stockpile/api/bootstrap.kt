package me.branchpanic.mods.stockpile.api

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import org.powermock.reflect.Whitebox

object TestItems {
    init {
        Whitebox.setInternalState(Items::class.java, "AIR", Item(Item.Settings()))
    }

    val StandardItemA = Item(Item.Settings())
    val StandardItemB = Item(Item.Settings())
}

infix operator fun Item.times(amount: Int) = ItemStack(this, amount)