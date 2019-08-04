package me.branchpanic.mods.stockpile

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import org.powermock.reflect.Whitebox

object TestItems {
    init {
        Whitebox.setInternalState(Items::class.java, "AIR", object : Item(Settings()) {
            override fun toString(): String = "Air"
        })
    }

    val ItemA = object : Item(Settings()) {
        override fun toString(): String = "Item A"
    }

    val ItemB = object : Item(Settings()) {
        override fun toString(): String = "Item B"
    }
}

infix fun Int.of(item: Item) = ItemStack(item, this)

fun ItemStack.withDummyTag(): ItemStack {
    this.tag = CompoundTag()
    return this
}
