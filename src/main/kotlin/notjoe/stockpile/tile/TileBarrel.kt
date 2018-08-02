package notjoe.stockpile.tile

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
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
        if (heldItem.isEmpty) {
            return
        }

        if (barrelInventory.typeIsUndefined) {
            barrelInventory.stackType = heldItem.withCount(1)
        }

        if (playerDoubleClicked(player)) {
            for (i in 0 until player.inventory.sizeInventory) {
                val currentStack = player.inventory.getStackInSlot(i)
                player.inventory.setInventorySlotContents(i, barrelInventory.insertStack(currentStack))
            }
        } else {
            player.setHeldItem(EnumHand.MAIN_HAND, barrelInventory.insertStack(heldItem))
            rightClickCache += player.uniqueID to System.currentTimeMillis()
        }

        markDirty()
    }

    override fun markDirty() {
        barrelInventory.markDirty()
        super.markDirty()
    }
}