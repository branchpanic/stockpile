package notjoe.stockpile.util.nbt

import net.minecraft.nbt.NBTTagCompound

/**
 * Represents an object that can be saved to and loaded from an NBTTagCompound in-place.
 */
interface NBTSavable {
    fun saveToCompound(): NBTTagCompound
    fun loadFromCompound(compound: NBTTagCompound)
}