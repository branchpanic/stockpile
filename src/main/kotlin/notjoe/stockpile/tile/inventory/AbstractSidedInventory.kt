package notjoe.stockpile.tile.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ISidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString

/**
 * A partial implementation of ISidedInventory that implements some of the methods we don't really need.
 */
abstract class AbstractSidedInventory(private val name: String) : ISidedInventory {
    // For the sake of having one abstract IInventory, ISidedInventory methods are implemented with placeholders that
    // make it behave like a normal IInventory.
    override fun getSlotsForFace(p0: EnumFacing?): IntArray = (0 until sizeInventory).toList().toIntArray()

    override fun canInsertItem(p0: Int, p1: ItemStack?, p2: EnumFacing?): Boolean = isItemValidForSlot(p0, p1)
    override fun canExtractItem(p0: Int, p1: ItemStack?, p2: EnumFacing?): Boolean = true

    // Inventory fields aren't used by Stockpile (for now, at least)
    override fun getFieldCount(): Int = 0

    override fun getField(fieldIndex: Int): Int = -1
    override fun setField(fieldIndex: Int, fieldValue: Int) {}

    // Containers usually won't have visible names.
    override fun hasCustomName(): Boolean = false

    override fun getCustomName(): ITextComponent? = null
    override fun getName(): ITextComponent = TextComponentString(name)

    // Container access methods default to NO-OPs Stockpile doesn't use many GUIs.
    override fun openInventory(player: EntityPlayer?) {}

    override fun closeInventory(player: EntityPlayer?) {}
    override fun isUsableByPlayer(player: EntityPlayer?): Boolean = true

    // Default to a NO-OP, since markDirty is generally handled by the associated TileEntity.
    override fun markDirty() {}
}
