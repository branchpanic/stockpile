package notjoe.stockpile.tile

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.EnumHand
import notjoe.stockpile.tile.inventory.MutableMassItemStorage
import notjoe.stockpile.tile.inventory.withCount
import java.util.*

const val BARREL_DOUBLE_CLICK_TIME_MS = 500

class TileBarrel(private var barrelInventory: MutableMassItemStorage = MutableMassItemStorage(ItemStack.EMPTY, 32)) :
        TileEntity(TYPE),
        IInventory by barrelInventory {

    companion object {
        lateinit var TYPE: TileEntityType<TileBarrel>
    }

    val stackType get() = barrelInventory.stackType
    var rightClickCache = emptyMap<UUID, Long>()

    private fun playerDoubleClicked(player: EntityPlayer): Boolean {
        val currentTime = System.currentTimeMillis()

        rightClickCache = rightClickCache.filterValues { clickedTime ->
            currentTime - clickedTime <= BARREL_DOUBLE_CLICK_TIME_MS
        }

        return player.uniqueID in rightClickCache
    }

    fun handleRightClick(player: EntityPlayer) {
        val heldItem = player.heldItemMainhand

        if (barrelInventory.typeIsUndefined && !heldItem.isEmpty) {
            barrelInventory.stackType = heldItem.withCount(1)
        } else {
            if (playerDoubleClicked(player)) {
                for (i in 0 until player.inventory.sizeInventory) {
                    val currentStack = player.inventory.getStackInSlot(i)
                    player.inventory.setInventorySlotContents(i, barrelInventory.insertStack(currentStack))
                }
            } else {
                val resultingStack = barrelInventory.insertStack(heldItem)
                player.setHeldItem(EnumHand.MAIN_HAND, resultingStack)
                rightClickCache += player.uniqueID to System.currentTimeMillis()
            }
        }

        markDirty()
    }

    override fun markDirty() {
        barrelInventory.markDirty()
        world.notifyBlockUpdate(pos, blockState, blockState, 3)
        super.markDirty()
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        return SPacketUpdateTileEntity(pos, 1, updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(NBTTagCompound())
    }

    override fun writeToNBT(compound: NBTTagCompound?): NBTTagCompound {
        val workingCompound = super.writeToNBT(compound)
        workingCompound.setTag("Inventory", barrelInventory.saveToCompound())
        return workingCompound
    }

    override fun readFromNBT(compound: NBTTagCompound?) {
        super.readFromNBT(compound)
        if (compound != null && compound.hasKey("Inventory")) {
            barrelInventory.loadFromCompound(compound.getCompoundTag("Inventory"))
        }
    }
}