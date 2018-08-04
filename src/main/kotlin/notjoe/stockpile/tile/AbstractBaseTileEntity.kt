package notjoe.stockpile.tile

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import notjoe.stockpile.tile.nbt.NBTSerializableInPlace
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A base tile entity for Stockpile, which
 */
abstract class AbstractBaseTileEntity(type: TileEntityType<*>?) : TileEntity(type) {
    private var persistentValues = emptyList<NBTProperty<*>>()

    // In our case, markDirty basically just means "sync now." This will probably change in the future.
    override fun markDirty() {
        super.markDirty()
        world.notifyBlockUpdate(pos, blockState, blockState, 3)
    }

    /**
     * Allows for an NBTSerializableInPlace value to persist (via writeToNBT and readFromNBT) automatically.
     */
    fun <T : NBTSerializableInPlace> persistent(name: String, initialValue: T): NBTProperty<T> {
        val delegate = NBTProperty(name, initialValue)
        persistentValues += delegate
        return delegate
    }


    override fun writeToNBT(compound: NBTTagCompound?): NBTTagCompound {
        val workingCompound = super.writeToNBT(compound) ?: NBTTagCompound()
        return writePersistentValuesToNBT(workingCompound)
    }

    fun writePersistentValuesToNBT(compound: NBTTagCompound): NBTTagCompound {
        persistentValues.forEach { delegate ->
            compound.setTag(delegate.name, delegate.loadedValue.saveToCompound())
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
                .forEach { delegate -> delegate.loadedValue.loadFromCompound(compound.getCompoundTag(delegate.name)) }
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        return SPacketUpdateTileEntity(pos, 1, updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(NBTTagCompound())
    }

    class NBTProperty<T : NBTSerializableInPlace>(val name: String,
                                                  internal var loadedValue: T) : ReadWriteProperty<TileEntity, T> {
        override operator fun getValue(thisRef: TileEntity, property: KProperty<*>): T = loadedValue
        override fun setValue(thisRef: TileEntity, property: KProperty<*>, value: T) {
            loadedValue = value
            thisRef.markDirty()
        }
    }
}