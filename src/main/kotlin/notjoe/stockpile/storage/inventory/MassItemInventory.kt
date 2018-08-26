package notjoe.stockpile.storage.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import notjoe.stockpile.util.ext.isStackableWith
import notjoe.stockpile.util.ext.withCount
import notjoe.stockpile.util.nbt.NBTSerializable
import kotlin.math.min

const val MASS_INVENTORY_OUTPUT_SLOT = 0
const val MASS_INVENTORY_INPUT_SLOT = 1

/**
 * Inventory implementation for storing a large amount of a single ItemStack.
 *
 * Contains two slots: one for output, and one for input (see constants MASS_INVENTORY_INPUT_SLOT and
 * MASS_INVENTORY_OUTPUT_SLOT). The output slot always supplies as much of the contained item as possible. The input slot
 * is empty until the amount reaches (maxStacks - 1) * stackLimit, in which it then contains the remaining stack.
 *
 * This implementation allows for the stack type to be mutated in-place. The method markDirty() is called whenever
 * changes are made.
 */
class MassItemInventory(
    initialStackType: ItemStack = ItemStack.EMPTY,
    var maxStacks: Int,
    var amount: Int = 0
) :
    AbstractSidedInventory("mass_item_storage"), NBTSerializable {

    companion object {
        const val STACK_TYPE_KEY = "StackType"
        const val MAX_STACKS_KEY = "MaxStacks"
        const val AMOUNT_KEY = "Amount"
        const val TYPE_LOCKED_KEY = "TypeIsLocked"
    }

    var stackType: ItemStack = initialStackType
        set(value) {
            field = value
            amount = 0
        }

    val availableSpace: Int get() = (maxStacks * inventoryStackLimit) - amount
    val typeCannotBeChanged: Boolean get() = !stackType.isEmpty && (typeIsLocked || !isEmpty)

    var typeIsLocked: Boolean = false
        set(locked) {
            if (!locked && isEmpty) {
                stackType = ItemStack.EMPTY
            }

            field = locked
        }

    /**
     * Inserts an ItemStack into this MassItemInventory and returns the remainder.
     */
    fun insertStack(stack: ItemStack): ItemStack {
        if (!isItemValidForSlot(MASS_INVENTORY_INPUT_SLOT, stack)) {
            return stack
        }

        val insertableAmount = min(stack.count, availableSpace)
        val remainderAmount = stack.count - insertableAmount

        setInventorySlotContents(MASS_INVENTORY_INPUT_SLOT, stack)

        return if (remainderAmount > 0) {
            stack.withCount(remainderAmount)
        } else {
            ItemStack.EMPTY
        }
    }

    override fun getStackInSlot(slotIndex: Int): ItemStack {
        if (slotIndex == MASS_INVENTORY_INPUT_SLOT) {
            val lastStackAmount = amount - ((maxStacks - 1) * inventoryStackLimit)
            if (lastStackAmount > 0) {
                return stackType.withCount(lastStackAmount)
            }
        }

        if (slotIndex == MASS_INVENTORY_OUTPUT_SLOT) {
            val availableStackAmount = min(amount, inventoryStackLimit)
            if (availableStackAmount > 0) {
                return stackType.withCount(availableStackAmount)
            }
        }

        return ItemStack.EMPTY
    }

    override fun removeStackFromSlot(slotIndex: Int): ItemStack {
        val workingStack = getStackInSlot(slotIndex)
        if (workingStack.isEmpty) {
            return ItemStack.EMPTY
        }

        amount -= workingStack.count

        markDirty()
        return workingStack
    }

    override fun clear() {
        amount = 0
        markDirty()
    }

    override fun getInventoryStackLimit(): Int = stackType.maxStackSize

    override fun decrStackSize(slotIndex: Int, decrementAmount: Int): ItemStack {
        val workingStack = getStackInSlot(slotIndex)
        if (workingStack.isEmpty) {
            return ItemStack.EMPTY
        }

        val decrementedAmount = min(workingStack.count, decrementAmount)
        amount -= decrementedAmount

        markDirty()
        return workingStack.withCount(decrementedAmount)
    }

    override fun getSizeInventory(): Int = 2

    override fun isEmpty(): Boolean = amount == 0

    override fun isItemValidForSlot(slotIndex: Int, stack: ItemStack?): Boolean {
        return stack != null && !stack.isEmpty && slotIndex == MASS_INVENTORY_INPUT_SLOT &&
            (!typeCannotBeChanged || isEmpty && !typeIsLocked || stack.isStackableWith(stackType))
    }

    override fun setInventorySlotContents(slotIndex: Int, stack: ItemStack?) {
        if (slotIndex != MASS_INVENTORY_INPUT_SLOT || stack == null || stack.isEmpty) {
            return
        }

        if (!typeCannotBeChanged || isEmpty && !typeIsLocked) {
            stackType = stack
        }

        amount += min(availableSpace, stack.count)

        markDirty()
    }

    override fun saveToCompound(): NBTTagCompound {
        val compound = NBTTagCompound()
        compound.setTag(STACK_TYPE_KEY, stackType.writeToNBT(NBTTagCompound()))
        compound.setInteger(MAX_STACKS_KEY, maxStacks)
        compound.setInteger(AMOUNT_KEY, amount)
        compound.setBoolean(TYPE_LOCKED_KEY, typeIsLocked)
        return compound
    }

    override fun loadFromCompound(compound: NBTTagCompound) {
        stackType = ItemStack.loadFromNBT(compound.getCompoundTag(STACK_TYPE_KEY))
        maxStacks = compound.getInteger(MAX_STACKS_KEY)
        amount = compound.getInteger(AMOUNT_KEY)
        typeIsLocked = compound.getBoolean(TYPE_LOCKED_KEY)
    }
}
