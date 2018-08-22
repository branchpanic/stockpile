package notjoe.stockpile.tile

import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.text.TextComponentTranslation
import notjoe.stockpile.tile.inventory.BARREL_OUTPUT_SLOT_INDEX
import notjoe.stockpile.tile.inventory.MutableMassItemStorage
import notjoe.stockpile.util.ext.withCount
import java.util.UUID

const val BARREL_DOUBLE_CLICK_TIME_MS = 500
const val BARREL_MAX_STACK_CAPACITY = 16777216 // 2^24 stacks, which yields a capacity of 2^30 items

/**
 * A (JABBA|YABBA|Storage Drawers|etc.)-inspired container which allows for storing a large amount of a single item.
 */
class TileBarrel(barrelInventory: MutableMassItemStorage = MutableMassItemStorage(ItemStack.EMPTY, 32)) :
    AbstractPersistentTileEntity(TileBarrel.TYPE),
    IInventory by barrelInventory {

    companion object Type {
        lateinit var TYPE: TileEntityType<TileBarrel>
    }

    private var barrelInventory by nbtBacked("Inventory", barrelInventory)
    private var rightClickCache = emptyMap<UUID, Long>()

    val stackType get() = barrelInventory.stackType
    val amountStored get() = barrelInventory.amount
    val maxStacks get() = barrelInventory.maxStacks

    fun clearStackType() {
        barrelInventory.stackType = ItemStack.EMPTY
    }

    /**
     * Determines whether or not a player double-clicked this BARREL.
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
     * - A single right-click attempts to insert the held item into this BARREL.
     * - A double right-click attempts to insert as many stacks as possible from the player's inventory into this
     *   barrel.
     */
    fun handleRightClick(player: EntityPlayer) {
        val heldStack = player.heldItemMainhand

        if (!barrelInventory.typeIsDefined && !heldStack.isEmpty) {
            barrelInventory.stackType = heldStack.withCount(1)
            markDirty()
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

        displayBarrelContents(player)
    }

    /**
     * Handles a left-click by a player.
     *
     * - Normal left-clicking will attempt to extract a single item from this BARREL.
     * - Crouch-left-clicking will attempt to remove an entire stack from this BARREL.
     */
    fun handleLeftClick(player: EntityPlayer) {
        val amountToExtract = if (player.isSneaking) inventoryStackLimit else 1
        val extractedStack = decrStackSize(BARREL_OUTPUT_SLOT_INDEX, amountToExtract)

        if (!extractedStack.isEmpty) {
            val allItemsGiven = player.addItemStackToInventory(extractedStack)
            if (!allItemsGiven) {
                world.spawnEntity(EntityItem(world, player.posX, player.posY, player.posZ, extractedStack))
            }

            player.inventory.markDirty()
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.1f, 0.7f)
        }

        displayBarrelContents(player)
        markDirty()
    }

    private fun displayBarrelContents(player: EntityPlayer) {
        if (!isEmpty) {
            player.sendStatusMessage(
                TextComponentTranslation(
                    "stockpile.barrel.contents_world",
                    "%,d".format(amountStored),
                    "%,d".format(maxStacks * inventoryStackLimit),
                    stackType.item.name.unformattedComponentText,
                    "%,d".format(amountStored / inventoryStackLimit),
                    "%,d".format(maxStacks)
                ), true
            )
        }
    }

    /**
     * Attempts to insert a player's held stack into this BARREL. Updates the player's held item if successful.
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
     * Attempts to insert every single stack from the player's barrelInventory into this BARREL. Updates the
     * barrelInventory as appropriate.
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
     * In order to address the diamond problem, we need to explicitly specify that we want AbstractBaseTileEntity's
     * version of markDirty.
     */
    override fun markDirty() {
        super.markDirty()
        world.notifyNeighborsOfStateChange(pos, blockState.block)
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        return SPacketUpdateTileEntity(pos, 1, updateTag)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(NBTTagCompound())
    }
}