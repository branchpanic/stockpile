package notjoe.stockpile.tile.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import notjoe.stockpile.util.nbt.NBTSerializableInPlace
import notjoe.stockpile.util.ext.isStackableWith
import notjoe.stockpile.util.ext.withCount
import kotlin.math.min

const val BARREL_OUTPUT_SLOT_INDEX = 0
const val BARREL_INPUT_SLOT_INDEX = 1

/**
 * Inventory implementation for storing a large amount of a single ItemStack.
 *
 * Contains two slots: one for output, and one for input (see constants BARREL_INPUT_SLOT_INDEX and
 * BARREL_OUTPUT_SLOT_INDEX). The output slot always supplies as much of the contained item as possible. The input slot
 * is empty until the amount reaches (maxStacks - 1) * stackLimit, in which it then contains the remaining stack.
 *
 * This implementation allows for the stack type to be mutated in-place. The method markDirty() is called whenever
 * changes are made.
 */
class MutableMassItemStorage(private var _stackType: ItemStack,
                             var maxStacks: Int,
                             var amount: Int = 0) :
        AbstractSidedInventory("mass_item_storage"), NBTSerializableInPlace {

    companion object TagNames {
        const val STACK_TYPE_KEY = "StackType"
        const val MAX_STACKS_KEY = "MaxStacks"
        const val AMOUNT_KEY = "Amount"
    }

    var stackType: ItemStack
        get() = _stackType
        set(value) {
            _stackType = value
            amount = 0
        }

    private val availableSpace: Int get() = (maxStacks * inventoryStackLimit) - amount
    val typeIsDefined: Boolean get() = !stackType.isEmpty

    /**
     * Inserts an ItemStack into this MutableMassItemStorage and returns the remainder.
     */
    fun insertStack(stack: ItemStack): ItemStack {
        if (!isItemValidForSlot(BARREL_INPUT_SLOT_INDEX, stack)) {
            return stack
        }

        val insertableAmount = min(stack.count, availableSpace)
        val remainderAmount = stack.count - insertableAmount
        setInventorySlotContents(BARREL_INPUT_SLOT_INDEX, stack)

        return if (remainderAmount > 0) {
            stack.withCount(remainderAmount)
        } else {
            ItemStack.EMPTY
        }
    }

    override fun getStackInSlot(slotIndex: Int): ItemStack {
        if (slotIndex == BARREL_INPUT_SLOT_INDEX) {
            val lastStackAmount = amount - ((maxStacks - 1) * inventoryStackLimit)
            if (lastStackAmount > 0) {
                return stackType.withCount(lastStackAmount)
            }
        }

        if (slotIndex == BARREL_OUTPUT_SLOT_INDEX) {
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
        return typeIsDefined && stack != null && !stack.isEmpty &&
                slotIndex == BARREL_INPUT_SLOT_INDEX && stackType.isStackableWith(stack)
    }

    override fun setInventorySlotContents(slotIndex: Int, stack: ItemStack?) {
        if (slotIndex != BARREL_INPUT_SLOT_INDEX || stack == null || stack.isEmpty) {
            return
        }

        amount += min(availableSpace, stack.count)

        markDirty()
    }

    override fun saveToCompound(): NBTTagCompound {
        val compound = NBTTagCompound()
        compound.setTag(STACK_TYPE_KEY, stackType.writeToNBT(NBTTagCompound()))
        compound.setInteger(MAX_STACKS_KEY, maxStacks)
        compound.setInteger(AMOUNT_KEY, amount)
        return compound
    }

    override fun loadFromCompound(compound: NBTTagCompound) {
        stackType = ItemStack.loadFromNBT(compound.getCompoundTag(STACK_TYPE_KEY))
        maxStacks = compound.getInteger(MAX_STACKS_KEY)
        amount = compound.getInteger(AMOUNT_KEY)
    }
}

