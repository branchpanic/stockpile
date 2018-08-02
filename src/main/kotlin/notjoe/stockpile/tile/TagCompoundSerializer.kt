package notjoe.stockpile.tile

import net.minecraft.nbt.NBTTagCompound

interface TagCompoundSerializer<T> {
    fun fromCompound(nbt: NBTTagCompound): T
    fun toCompound(t: T): NBTTagCompound
}