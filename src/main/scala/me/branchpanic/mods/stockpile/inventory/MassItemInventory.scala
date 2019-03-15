package me.branchpanic.mods.stockpile.inventory

import me.branchpanic.mods.stockpile.blockentity.Persistence
import me.branchpanic.mods.stockpile.extension.ItemStackExtensions._
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction

object MassItemInventory {
  val MAX_CAPACITY_STACKS = 16777216
  val DEFAULT_CAPACITY_STACKS = 32
  val OUTPUT_SLOT_INDEX = 0
  val INPUT_SLOT_INDEX = 1
}

/**
 * An Inventory implementation which stores an arbitrary amount of a single ItemStack.
 *
 * @param _stackType             ItemStack type stored, or EMPTY if none is defined.
 * @param _amountStored           Amount of the ItemStack currently stored (individual items, not stacks).
 * @param maxStacks              The maximum number of *stacks* that this MassItemInventory can hold. This is affected
 *                               by the maximum stack size of the current _stackType.
 * @param _acceptNewStackWhenEmpty Whether or not *any* stack will be accepted if this inventory is currently empty.
 * @param onChanged              Action to perform when a change has been made.
 */
class MassItemInventory(private[this] var _stackType: ItemStack = ItemStack.EMPTY,
                        private[this] var _amountStored: Int = 0,
                        private[this] var _acceptNewStackWhenEmpty: Boolean = true,
                        var maxStacks: Int = MassItemInventory.DEFAULT_CAPACITY_STACKS,
                        val onChanged: () => Unit = () => {})
    extends SidedInventory
    with Persistence {

  def stackType: ItemStack = _stackType
  def stackSize: Int = _stackType.getMaxAmount
  def availableSpace: Int = (stackSize * maxStacks) - _amountStored
  def amountStored: Int = _amountStored
  def isAcceptingNewStackWhenEmpty: Boolean = _acceptNewStackWhenEmpty

  def invertEmptyStackBehavior(): Unit =
    _acceptNewStackWhenEmpty = !_acceptNewStackWhenEmpty

  /**
   * Inserts the largest portion possible of the supplied ItemStack into this MassItemInventory.
   *
   * @param itemStack ItemStack to insert.
   * @return The portion of the ItemStack that couldn't be inserted.
   */
  def insertStack(itemStack: ItemStack): ItemStack = {
    if (!isValidInvStack(MassItemInventory.INPUT_SLOT_INDEX, itemStack) && !isAcceptingNewStackType) {
      return itemStack
    }

    val insertableAmount = Math.min(itemStack.getAmount, availableSpace)
    val remainingAmount = itemStack.getAmount - insertableAmount

    setInvStack(MassItemInventory.INPUT_SLOT_INDEX, itemStack.withAmount(insertableAmount))

    itemStack.withAmount(remainingAmount)
  }

  def isAcceptingNewStackType: Boolean =
    _stackType.isEmpty || (_acceptNewStackWhenEmpty && isInvEmpty)

  override def isValidInvStack(slotIndex: Int, stack: ItemStack): Boolean =
    slotIndex == MassItemInventory.INPUT_SLOT_INDEX && (isAcceptingNewStackType || ItemStack
      .areEqual(stack.withAmount(1), _stackType))

  override def getInvSize: Int = 2

  override def isInvEmpty: Boolean = _amountStored == 0 || _stackType.isEmpty

  override def setInvStack(slotIndex: Int, itemStack: ItemStack): Unit = {
    if (slotIndex != MassItemInventory.INPUT_SLOT_INDEX || itemStack.isEmpty) {
      return
    }

    if (isAcceptingNewStackType) {
      _stackType = itemStack.withAmount(1)
    }

    _amountStored += Math.min(availableSpace, itemStack.getAmount)
    markDirty()
  }

  override def takeInvStack(slotIndex: Int, amount: Int): ItemStack = {
    val workingStack = getInvStack(slotIndex)

    if (workingStack.isEmpty) {
      return ItemStack.EMPTY
    }

    val decrementAmount = Math.min(workingStack.getAmount, amount)
    _amountStored -= decrementAmount
    markDirty()
    workingStack.withAmount(decrementAmount)
  }

  override def removeInvStack(slotIndex: Int): ItemStack = {
    val workingStack = getInvStack(slotIndex)

    if (workingStack.isEmpty) {
      ItemStack.EMPTY
    }

    _amountStored -= workingStack.getAmount
    markDirty()
    workingStack
  }

  override def getInvStack(slotIndex: Int): ItemStack = slotIndex match {
    case MassItemInventory.INPUT_SLOT_INDEX =>
      val overflowStackAmount = _amountStored - ((maxStacks - 1) * stackSize)
      if (overflowStackAmount > 0) {
        _stackType.withAmount(overflowStackAmount)
      } else {
        ItemStack.EMPTY
      }

    case MassItemInventory.OUTPUT_SLOT_INDEX =>
      val outputStackAmount = Math.min(_amountStored, stackSize)
      if (outputStackAmount > 0) {
        _stackType.withAmount(outputStackAmount)
      } else {
        ItemStack.EMPTY
      }

    case _ =>
      ItemStack.EMPTY
  }

  override def markDirty(): Unit =
    onChanged()

  override def canPlayerUseInv(playerEntity: PlayerEntity): Boolean = true

  override def clear(): Unit = {
    _stackType = ItemStack.EMPTY
    _amountStored = 0
  }

  override def getInvAvailableSlots(direction: Direction): Array[Int] =
    Array(MassItemInventory.INPUT_SLOT_INDEX, MassItemInventory.OUTPUT_SLOT_INDEX)

  override def canInsertInvStack(i: Int, stack: ItemStack, direction: Direction): Boolean =
    isValidInvStack(i, stack)

  override def canExtractInvStack(i: Int, stack: ItemStack, direction: Direction): Boolean =
    i == MassItemInventory.OUTPUT_SLOT_INDEX

  override def saveToTag(): CompoundTag = {
    val tag = new CompoundTag()
    tag.putBoolean("allowNewStackWhenEmpty", _acceptNewStackWhenEmpty)
    tag.putInt("maxStacks", maxStacks)
    tag.putInt("amountStored", _amountStored)
    tag.put("_stackType", _stackType.toTag(new CompoundTag))
    tag
  }

  override def loadFromTag(tag: CompoundTag): Unit = {
    _acceptNewStackWhenEmpty = tag.getBoolean("allowNewStackWhenEmpty")
    maxStacks = tag.getInt("maxStacks")
    _amountStored = tag.getInt("amountStored")
    _stackType = ItemStack.fromTag(tag.getCompound("_stackType"))
  }
}
