package notjoe.stockpile.tile

import net.minecraft.nbt.NBTTagCompound

interface SerializableInPlace {
    fun saveToCompound(): NBTTagCompound
    fun loadFromCompound(compound: NBTTagCompound)
}