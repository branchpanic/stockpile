package notjoe.stockpile.tile.inventory

import net.minecraft.item.ItemStack

/**
 * An inventory that can always accept items, which aren't actually stored anywhere and are thus effectively destroyed.
 */
class VoidInventory : AbstractSidedInventory("void") {
    override fun getStackInSlot(p0: Int): ItemStack = ItemStack.EMPTY
    override fun clear() {}
    override fun getInventoryStackLimit(): Int = ItemStack.EMPTY.maxStackSize
    override fun removeStackFromSlot(slotIndex: Int): ItemStack = ItemStack.EMPTY
    override fun decrStackSize(slotIndex: Int, amount: Int): ItemStack = ItemStack.EMPTY
    override fun getSizeInventory(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun isItemValidForSlot(p0: Int, p1: ItemStack?): Boolean = true
    override fun setInventorySlotContents(p0: Int, p1: ItemStack?) {}
}