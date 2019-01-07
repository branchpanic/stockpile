package notjoe.stockpile.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.{StringTextComponent, TextComponent}
import net.minecraft.util.math.Direction
import notjoe.stockpile.blockentity.AutoPersistence.PersistentField
import notjoe.stockpile.extension.ItemStackExtensions._

object MassItemInventory {
  val MaxCapacityStacks = 16777216
  val DefaultCapacityStacks = 32
  val OutputSlotIndex = 0
  val InputSlotIndex = 1
}

/**
  * An Inventory implementation which stores an arbitrary amount of a single ItemStack.
  *
  * @param _stackType              ItemStack type stored, or EMPTY if none is defined.
  * @param amountStored           Amount of the ItemStack currently stored (individual items, not stacks).
  * @param maxStacks              The maximum number of *stacks* that this MassItemInventory can hold. This is affected
  *                               by the maximum stack size of the current _stackType.
  * @param allowNewStackWhenEmpty Whether or not *any* stack will be accepted if this inventory is currently empty.
  * @param name                   Inventory name.
  * @param onChanged              Action to perform when a change has been made.
  */
class MassItemInventory(@PersistentField private var _stackType: ItemStack = ItemStack.EMPTY,
                        @PersistentField var amountStored: Int = 0,
                        @PersistentField var maxStacks: Int = MassItemInventory.DefaultCapacityStacks,
                        @PersistentField var allowNewStackWhenEmpty: Boolean = true,
                        @PersistentField var name: String = "Barrel",
                        val onChanged: () => Unit = () => {}) extends SidedInventory {

  def stackType: ItemStack = _stackType

  def stackSize: Int = _stackType.getMaxAmount
  def availableSpace: Int = (stackSize * maxStacks) - amountStored

  /**
    * Inserts the largest portion possible of the supplied ItemStack into this MassItemInventory.
    *
    * @param itemStack ItemStack to insert.
    * @return The portion of the ItemStack that couldn't be inserted.
    */
  def insertStack(itemStack: ItemStack): ItemStack = {
    if (!isValidInvStack(MassItemInventory.InputSlotIndex, itemStack) && !isAcceptingNewStackType) {
      itemStack
    } else {
      val insertableAmount = Math.min(itemStack.getAmount, availableSpace)
      val remainingAmount = itemStack.getAmount - insertableAmount

      setInvStack(MassItemInventory.InputSlotIndex, itemStack.withAmount(insertableAmount))

      itemStack.withAmount(remainingAmount)
    }
  }

  def isAcceptingNewStackType: Boolean = _stackType.isEmpty || (allowNewStackWhenEmpty && isInvEmpty)

  override def isValidInvStack(slotIndex: Int, stack: ItemStack): Boolean =
    slotIndex == MassItemInventory.InputSlotIndex && (isAcceptingNewStackType || ItemStack.areEqual(stack.withAmount(1), _stackType))

  override def getInvSize: Int = 2

  override def isInvEmpty: Boolean = amountStored == 0 || _stackType.isEmpty

  override def getInvStack(slotIndex: Int): ItemStack = slotIndex match {
    case MassItemInventory.InputSlotIndex =>
      val overflowStackAmount = amountStored - ((maxStacks - 1) * stackSize)
      if (overflowStackAmount > 0) {
        _stackType.withAmount(overflowStackAmount)
      } else {
        ItemStack.EMPTY
      }

    case MassItemInventory.OutputSlotIndex =>
      val outputStackAmount = Math.min(amountStored, stackSize)
      if (outputStackAmount > 0) {
        _stackType.withAmount(outputStackAmount)
      } else {
        ItemStack.EMPTY
      }

    case _ =>
      ItemStack.EMPTY
  }

  override def takeInvStack(slotIndex: Int, amount: Int): ItemStack = {
    val workingStack = getInvStack(slotIndex)

    if (workingStack.isEmpty) {
      ItemStack.EMPTY
    } else {
      val decrementAmount = Math.min(workingStack.getAmount, amount)
      amountStored -= decrementAmount
      markDirty()
      workingStack.withAmount(decrementAmount)
    }
  }

  override def removeInvStack(slotIndex: Int): ItemStack = {
    val workingStack = getInvStack(slotIndex)

    if (workingStack.isEmpty) {
      ItemStack.EMPTY
    } else {
      amountStored -= workingStack.getAmount
      markDirty()
      workingStack
    }
  }

  override def setInvStack(slotIndex: Int, itemStack: ItemStack): Unit = {
    if (slotIndex != MassItemInventory.InputSlotIndex || itemStack.isEmpty) {
      return
    }

    if (isAcceptingNewStackType) {
      _stackType = itemStack.withAmount(1)
    }

    amountStored += Math.min(availableSpace, itemStack.getAmount)
    markDirty()
  }

  override def markDirty(): Unit = {
    onChanged()
  }

  override def canPlayerUseInv(playerEntity: PlayerEntity): Boolean = true

  override def getName: TextComponent = new StringTextComponent(name)

  override def clearInv(): Unit = {
    _stackType = ItemStack.EMPTY
    amountStored = 0
  }

  override def getInvAvailableSlots(direction: Direction): Array[Int] =
    Array(MassItemInventory.InputSlotIndex, MassItemInventory.OutputSlotIndex)

  override def canInsertInvStack(i: Int, stack: ItemStack, direction: Direction): Boolean = isValidInvStack(i, stack)

  override def canExtractInvStack(i: Int, stack: ItemStack, direction: Direction): Boolean =
    i == MassItemInventory.OutputSlotIndex
}
