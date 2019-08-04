package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text

class MassItemStackStorage(override var contents: Quantizer<ItemStack>, var maxStacks: Int) :
    MutableMassStorage<ItemStack> {
    override val capacity: Long
        get() = (maxStacks * contents.reference.maxCount).toLong()

    override fun describeContents(): Text {
        TODO("not implemented")
    }

    fun toCompoundTag(): CompoundTag {
        TODO()
    }

    fun fromCompoundTag(): CompoundTag {
        TODO()
    }
}
