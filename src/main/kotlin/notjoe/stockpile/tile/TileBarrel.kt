package notjoe.stockpile.tile

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import notjoe.stockpile.tile.inventory.MutableMassItemStorage
import notjoe.stockpile.tile.inventory.OUTPUT_SLOT_INDEX
import notjoe.stockpile.tile.inventory.withCount
import java.util.*

const val BARREL_DOUBLE_CLICK_TIME_MS = 500

/**
 * A (JABBA|YABBA|Storage Drawers|etc.)-inspired container which allows for storing a large amount of a single item.
 */
class TileBarrel(private val barrelInventory: MutableMassItemStorage = MutableMassItemStorage(ItemStack.EMPTY, 32)) :
        TileEntity(TileBarrel.TYPE),
        IInventory by barrelInventory {
    companion object {
        @JvmStatic
        lateinit var TYPE: TileEntityType<TileBarrel>
    }

    constructor(maxStacks: Int) : this(MutableMassItemStorage(ItemStack.EMPTY, maxStacks))

    val stackType get() = barrelInventory.stackType
    val amountStored get() = barrelInventory.amount

    var rightClickCache = emptyMap<UUID, Long>()

    /**
     * Determines whether or not a player double-clicked this barrel.
     */
    private fun playerDoubleRightClicked(player: EntityPlayer): Boolean {
        val currentTime = System.currentTimeMillis()

        rightClickCache = rightClickCache.filterValues { clickedTime ->
            currentTime - clickedTime <= BARREL_DOUBLE_CLICK_TIME_MS
        }

        return player.uniqueID in rightClickCache
    }

    /**
     * Handles a right-click (single or double) by a player.
     *
     * - A single right-click attempts to insert the held item into this barrel.
     * - A double right-click attempts to insert as many stacks as possible from the player's inventory into this
     *   barrel.
     */
    fun handleRightClick(player: EntityPlayer) {
        val heldItem = player.heldItemMainhand

        if (barrelInventory.typeIsUndefined && !heldItem.isEmpty) {
            barrelInventory.stackType = heldItem.withCount(1)
        } else {
            val changesMade = if (playerDoubleRightClicked(player)) {
                insertAllPossibleStacks(player)
            } else {
                insertStackFromHand(player, EnumHand.MAIN_HAND)
            }

            if (changesMade) {
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.1f, 0.9f)
                markDirty()
            }
        }
    }

    /**
     * Attempts to insert a player's held stack into this barrel. Updates the player's held item if successful.
     * @return Whether or not a change was made.
     */
    private fun insertStackFromHand(player: EntityPlayer, hand: EnumHand): Boolean {
        val heldItem = player.getHeldItem(hand)
        val resultingStack = barrelInventory.insertStack(heldItem)
        player.setHeldItem(EnumHand.MAIN_HAND, resultingStack)
        rightClickCache += player.uniqueID to System.currentTimeMillis()

        return resultingStack.count < heldItem.count
    }

    /**
     * Attempts to insert every single stack from the player's inventory into this barrel. Updates the inventory as
     * appropriate.
     * @return Whether or not any changes were made.
     */
    private fun insertAllPossibleStacks(player: EntityPlayer): Boolean {
        var somethingWasRemoved = false
        for (i in 0 until player.inventory.sizeInventory) {
            val currentStack = player.inventory.getStackInSlot(i)
            val resultingStack = barrelInventory.insertStack(currentStack)
            player.inventory.setInventorySlotContents(i, resultingStack)
            if (!somethingWasRemoved && resultingStack.count < currentStack.count) {
                somethingWasRemoved = true
            }
        }

        return somethingWasRemoved
    }

    /**
     * Handles a left-click by a player.
     *
     * - Normal left-clicking will attempt to extract a single item from this barrel.
     * - Crouch-left-clicking will attempt to remove an entire stack from this barrel.
     */
    fun handleLeftClick(player: EntityPlayer) {
        val amountToExtract = if (player.isSneaking) inventoryStackLimit else 1
        val extractedStack = decrStackSize(OUTPUT_SLOT_INDEX, amountToExtract)

        if (!extractedStack.isEmpty) {
            player.addItemStackToInventory(extractedStack)
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.1f, 0.7f)
        }

        markDirty()
    }

    override fun markDirty() {
        println("TileBarrel: Marked dirty")
        super.markDirty()
        println("TileBarrel: Notifying block update")
        world.notifyBlockUpdate(pos, blockState, blockState, 3)
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        println("TileBarrel: Synchronizing")
        return SPacketUpdateTileEntity(pos, 1, updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        println("TileBarrel: Getting update tag")
        return writeToNBT(NBTTagCompound())
    }

    override fun writeToNBT(compound: NBTTagCompound?): NBTTagCompound {
        println("TileBarrel: Writing")
        val workingCompound = super.writeToNBT(compound)
        workingCompound.setTag("Inventory", barrelInventory.saveToCompound())
        return workingCompound
    }

    override fun readFromNBT(compound: NBTTagCompound?) {
        println("TileBarrel: Reading")
        super.readFromNBT(compound)
        if (compound != null && compound.hasKey("Inventory")) {
            barrelInventory.loadFromCompound(compound.getCompoundTag("Inventory"))
        }
    }
}