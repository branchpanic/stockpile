package notjoe.stockpile.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.text.{StringTextComponent, TextComponent}
import notjoe.stockpile.blockentity.AutoPersistence.PersistentField
import notjoe.stockpile.extension.ItemStackExtensions._

object MassItemInventory {
  final val DEFAULT_MAX_STACKS = 16
  final val OUTPUT_SLOT = 0
  final val INPUT_SLOT = 1
}

/**
  * An Inventory implementation which stores an arbitrary amount of a single ItemStack.
  *
  * @param stackType              ItemStack type stored, or EMPTY if none is defined.
  * @param amountStored           Amount of the ItemStack currently stored (individual items, not stacks).
  * @param maxStacks              The maximum number of *stacks* that this MassItemInventory can hold. This is affected
  *                               by the maximum stack size of the current stackType.
  * @param allowNewStackWhenEmpty Whether or not *any* stack will be accepted if this inventory is currently empty.
  * @param name                   Inventory name.
  * @param onChanged              Action to perform when a change has been made.
  */
class MassItemInventory(@PersistentField var stackType: ItemStack = ItemStack.EMPTY,
                        @PersistentField var amountStored: Int = 0,
                        @PersistentField var maxStacks: Int = MassItemInventory.DEFAULT_MAX_STACKS,
                        @PersistentField var allowNewStackWhenEmpty: Boolean = true,
                        @PersistentField var name: String = "Barrel",
                        val onChanged: () => Unit) extends Inventory {

  override def toString: String = s"MassItemInventory{stackType=$stackType, amountStored=$amountStored, " +
    s"maxStacks=$maxStacks, allowNewStackWhenEmpty=$allowNewStackWhenEmpty, name=$name ... empty? $isInvEmpty isAcceptingNewStackType? $isAcceptingNewStackType}"

  def stackSize: Int = stackType.getMaxAmount

  def availableSpace: Int = stackSize * maxStacks

  /**
    * Inserts the largest portion possible of the supplied ItemStack into this MassItemInventory.
    *
    * @param itemStack ItemStack to insert.
    * @return The portion of the ItemStack that couldn't be inserted.
    */
  def insertStack(itemStack: ItemStack): ItemStack = {
    if (!isValidInvStack(MassItemInventory.INPUT_SLOT, itemStack) && !isAcceptingNewStackType) {
      itemStack
    } else {
      val insertableAmount = Math.min(itemStack.getAmount, availableSpace)
      val remainingAmount = itemStack.getAmount - insertableAmount

      setInvStack(MassItemInventory.INPUT_SLOT, itemStack)

      itemStack.withAmount(remainingAmount)
    }
  }

  def isAcceptingNewStackType: Boolean = stackType.isEmpty || (allowNewStackWhenEmpty && isInvEmpty)

  override def isValidInvStack(slotIndex: Int, stack: ItemStack): Boolean =
    slotIndex == MassItemInventory.INPUT_SLOT && ItemStack.areEqual(stack.withAmount(1), stackType)

  override def getInvSize: Int = 2

  override def isInvEmpty: Boolean = amountStored == 0 || stackType.isEmpty

  override def getInvStack(slotIndex: Int): ItemStack = slotIndex match {
    case MassItemInventory.INPUT_SLOT =>
      val overflowStackAmount = amountStored - ((maxStacks - 1) * stackSize)
      if (overflowStackAmount > 0) {
        stackType.withAmount(overflowStackAmount)
      } else {
        ItemStack.EMPTY
      }

    case MassItemInventory.OUTPUT_SLOT =>
      val outputStackAmount = Math.min(amountStored, stackSize)
      if (outputStackAmount > 0) {
        stackType.withAmount(outputStackAmount)
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
    if (slotIndex != MassItemInventory.INPUT_SLOT || itemStack.isEmpty) {
      return
    }

    if (isAcceptingNewStackType) {
      stackType = itemStack.withAmount(1)
    }

    amountStored += Math.min(availableSpace, itemStack.getAmount)
    markDirty()
  }

  override def markDirty(): Unit = onChanged()

  override def canPlayerUseInv(playerEntity: PlayerEntity): Boolean = true

  override def getName: TextComponent = new StringTextComponent(name)

  override def clearInv(): Unit = {
    stackType = ItemStack.EMPTY
    amountStored = 0
  }
}
