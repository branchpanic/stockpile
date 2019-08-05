package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class MassItemStackStorage(override var contents: Quantizer<ItemStack>, var maxStacks: Int) :
    MutableMassStorage<ItemStack> {
    override val capacity: Long
        get() = (maxStacks * contents.reference.maxCount).toLong()

    override fun describeContents(): Text =
        if (isEmpty) TranslatableText("ui.stockpile.barrel.contents")
        else TranslatableText("ui.stockpile.barrel.contents_empty")
}
