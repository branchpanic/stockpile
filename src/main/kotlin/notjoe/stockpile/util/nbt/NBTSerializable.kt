package notjoe.stockpile.util.nbt

import net.minecraft.nbt.NBTTagCompound

/**
 * Represents an object that can be saved to and loaded from an NBTTagCompound in-place.
 *
 * NOTE: In the future, it's very likely that this will be replaced by a library.
 */
interface NBTSerializable {
    fun saveToCompound(): NBTTagCompound
    fun loadFromCompound(compound: NBTTagCompound)
}