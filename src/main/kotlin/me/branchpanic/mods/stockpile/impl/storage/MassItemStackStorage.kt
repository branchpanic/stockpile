package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import me.branchpanic.mods.stockpile.api.storage.Quantifier
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import java.text.NumberFormat

class MassItemStackStorage(override var contents: Quantifier<ItemStack>, var maxStacks: Int) :
    MutableMassStorage<ItemStack> {
    override val capacity: Long
        get() = (maxStacks * contents.reference.maxCount).toLong()

    override fun describeContents(): Text =
        if (!isEmpty) TranslatableText(
            "ui.stockpile.barrel.contents",
            contents.amount.format(),
            capacity.format(),
            contents.reference.name,
            (contents.amount / contents.reference.maxCount).format(),
            maxStacks.format()
        ) else TranslatableText(
            "ui.stockpile.barrel.contents_empty",
            maxStacks.format()
        )
}

internal fun Long.format(): String = NumberFormat.getNumberInstance().format(this)
private fun Int.format(): String = NumberFormat.getNumberInstance().format(this)