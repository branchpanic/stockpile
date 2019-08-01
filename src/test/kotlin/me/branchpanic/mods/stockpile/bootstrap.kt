package me.branchpanic.mods.stockpile

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import org.powermock.reflect.Whitebox

object TestItems {
    init {
        Whitebox.setInternalState(Items::class.java, "AIR", Item(Item.Settings()))
    }

    val ItemA = Item(Item.Settings())
    val ItemB = Item(Item.Settings())
}

infix fun Int.of(item: Item) = ItemStack(item, this)

fun ItemStack.withDummyTag(): ItemStack {
    this.tag = CompoundTag()
    return this
}
