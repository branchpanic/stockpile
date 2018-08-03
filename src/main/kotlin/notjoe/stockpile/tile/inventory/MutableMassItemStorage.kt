package notjoe.stockpile.tile.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import notjoe.stockpile.tile.SerializableInPlace
import kotlin.math.min

const val OUTPUT_SLOT_INDEX = 0
const val INPUT_SLOT_INDEX = 1

/**
 * Inventory implementation for storing a large amount of a single ItemStack.
 *
 * Contains two slots: one for output, and one for input (see constants INPUT_SLOT_INDEX and OUTPUT_SLOT_INDEX). The
 * output slot always supplies as much of the contained item as possible. The input slot is empty until the amount
 * reaches (maxStacks - 1) * stackLimit, in which it then contains the remaining stack.
 *
 * This implementation allows for the stack type to be mutated in-place.
 */
class MutableMassItemStorage(private var _stackType: ItemStack,
                             private var maxStacks: Int,
                             var amount: Int = 0) :
        AbstractSidedInventory("mass_item_storage"), SerializableInPlace {

    var stackType: ItemStack
        get() = _stackType
        set(value) {
            _stackType = value
            amount = 0
        }

    val availableSpace: Int get() = (maxStacks * inventoryStackLimit) - amount
    val typeIsUndefined: Boolean get() = stackType.isEmpty

    /**
     * Inserts an ItemStack into this MutableMassItemStorage and returns the remainder.
     */
    fun insertStack(stack: ItemStack): ItemStack {
        if (!isItemValidForSlot(INPUT_SLOT_INDEX, stack)) {
            return stack
        }

        val insertableAmount = min(stack.count, availableSpace)
        val remainderAmount = stack.count - insertableAmount
        setInventorySlotContents(INPUT_SLOT_INDEX, stack)

        return if (remainderAmount > 0) {
            stack.withCount(remainderAmount)
        } else {
            ItemStack.EMPTY
        }
    }

    override fun getStackInSlot(slotIndex: Int): ItemStack {
        if (slotIndex == INPUT_SLOT_INDEX) {
            val lastStackAmount = amount - ((maxStacks - 1) * inventoryStackLimit)
            if (lastStackAmount > 0) {
                return stackType.withCount(lastStackAmount)
            }
        }

        if (slotIndex == OUTPUT_SLOT_INDEX) {
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
        return !typeIsUndefined && stack != null && !stack.isEmpty &&
                slotIndex == INPUT_SLOT_INDEX && stackType.isStackableWith(stack)
    }

    override fun setInventorySlotContents(slotIndex: Int, stack: ItemStack?) {
        if (slotIndex != INPUT_SLOT_INDEX || stack == null || stack.isEmpty) {
            return
        }

        amount += min(availableSpace, stack.count)
        markDirty()
    }

    override fun saveToCompound(): NBTTagCompound {
        val compound = NBTTagCompound()
        compound.setTag("StackType", stackType.writeToNBT(NBTTagCompound()))
        compound.setInteger("MaxStacks", maxStacks)
        compound.setInteger("Amount", amount)
        return compound
    }

    override fun loadFromCompound(compound: NBTTagCompound) {
        stackType = ItemStack.func_199557_a(compound.getCompoundTag("StackType"))
        maxStacks = compound.getInteger("MaxStacks")
        amount = compound.getInteger("Amount")
    }
}

// An adaptation of ItemStack::areItemStacksEqual which doesn't factor in quantity.
fun ItemStack.isStackableWith(other: ItemStack): Boolean {
    return if (item !== other.item) {
        false
    } else if (tagCompound == null && other.tagCompound != null) {
        false
    } else {
        tagCompound == null || this.tagCompound == other.tagCompound
    }
}