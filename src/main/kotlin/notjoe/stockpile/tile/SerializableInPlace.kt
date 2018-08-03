package notjoe.stockpile.tile

import net.minecraft.nbt.NBTTagCompound

/**
 * Interface representing an object that can be serialized in-place to and from an NBTTagCompound. Objects implementing
 * this interface must be mutable due to its nature.
 *
 * It would be nice to eliminate as much mutability as possible, but sometimes it's not practical in the context of this
 * mod. Suggestions for improvement are welcome!
 */
interface SerializableInPlace {
    fun saveToCompound(): NBTTagCompound
    fun loadFromCompound(compound: NBTTagCompound)
}