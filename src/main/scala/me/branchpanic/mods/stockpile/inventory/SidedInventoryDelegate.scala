package me.branchpanic.mods.stockpile.inventory

import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

trait SidedInventoryDelegate extends SidedInventory {
  val inventoryImpl: SidedInventory

  override def getInvSize: Int = inventoryImpl.getInvSize

  override def isInvEmpty: Boolean = inventoryImpl.isInvEmpty

  override def getInvStack(i: Int): ItemStack = inventoryImpl.getInvStack(i)

  override def takeInvStack(i: Int, i1: Int): ItemStack =
    inventoryImpl.takeInvStack(i, i1)

  override def isValidInvStack(int_1: Int, itemStack_1: ItemStack): Boolean =
    inventoryImpl.isValidInvStack(int_1, itemStack_1)

  override def removeInvStack(i: Int): ItemStack = inventoryImpl.removeInvStack(i)

  override def setInvStack(i: Int, itemStack: ItemStack): Unit =
    inventoryImpl.setInvStack(i, itemStack)

  override def getInvAvailableSlots(direction: Direction): Array[Int] =
    inventoryImpl.getInvAvailableSlots(direction)

  override def canInsertInvStack(i: Int, itemStack: ItemStack, direction: Direction): Boolean =
    inventoryImpl.canInsertInvStack(i, itemStack, direction)

  override def canExtractInvStack(i: Int, itemStack: ItemStack, direction: Direction): Boolean =
    inventoryImpl.canExtractInvStack(i, itemStack, direction)

  override def clear(): Unit = inventoryImpl.clear()
}
