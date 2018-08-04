package notjoe.stockpile.tile

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import notjoe.stockpile.tile.nbt.NBTSerializableInPlace
import kotlin.reflect.KProperty

/**
 * A base tile entity for Stockpile, which
 */
abstract class AbstractBaseTileEntity(type: TileEntityType<*>?) : TileEntity(type) {
    private var persistentValues = emptyList<NBTDelegate<*>>()

    // In our case, markDirty basically just means "sync now." This will probably change in the future.
    override fun markDirty() {
        super.markDirty()
        world.notifyBlockUpdate(pos, blockState, blockState, 3)
    }

    /**
     * Allows for an NBTSerializableInPlace value to persist (via writeToNBT and readFromNBT) automatically.
     */
    fun <T : NBTSerializableInPlace> persistent(name: String, initialValue: T): NBTDelegate<T> {
        val delegate = NBTDelegate(name, initialValue)
        persistentValues += delegate
        return delegate
    }


    override fun writeToNBT(compound: NBTTagCompound?): NBTTagCompound {
        val workingCompound = super.writeToNBT(compound) ?: NBTTagCompound()
        return writePersistentValuesToNBT(workingCompound)
    }

    fun writePersistentValuesToNBT(compound: NBTTagCompound): NBTTagCompound {
        persistentValues.forEach { delegate ->
            compound.setTag(delegate.name, delegate.serializable.saveToCompound())
        }

        return compound
    }

    override fun readFromNBT(compound: NBTTagCompound?) {
        if (compound == null) {
            return
        }

        super.readFromNBT(compound)
        readPersistentValuesFromNBT(compound)
    }

    fun readPersistentValuesFromNBT(compound: NBTTagCompound) {
        persistentValues
                .filter { delegate -> compound.hasKey(delegate.name) }
                .forEach { delegate -> delegate.serializable.loadFromCompound(compound.getCompoundTag(delegate.name)) }
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        return SPacketUpdateTileEntity(pos, 1, updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(NBTTagCompound())
    }
}

/**
 * A very simple wrapper for delegating a value to an NBT tag, ad-hoc to AbstractBaseTileEntity.
 */
class NBTDelegate<T : NBTSerializableInPlace>(val name: String, var serializable: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = serializable
}