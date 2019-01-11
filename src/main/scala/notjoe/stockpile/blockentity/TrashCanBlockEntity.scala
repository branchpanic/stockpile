package notjoe.stockpile.blockentity

import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BoundingBox, Direction}
import notjoe.stockpile.block.StockpileProperties

object TrashCanBlockEntity {
  val Type: BlockEntityType[TrashCanBlockEntity] =
    BlockEntityType.Builder.create[TrashCanBlockEntity](() => new TrashCanBlockEntity).method_11034(null)
}

class TrashCanBlockEntity extends BlockEntity(TrashCanBlockEntity.Type) with SidedInventory with Tickable {
  override def getInvAvailableSlots(direction: Direction): Array[Int] = Array(1)

  override def canInsertInvStack(i: Int, itemStack: ItemStack, direction: Direction): Boolean = true

  override def canExtractInvStack(i: Int, itemStack: ItemStack, direction: Direction): Boolean = false

  override def getInvSize: Int = 1

  override def isInvEmpty: Boolean = true

  override def getInvStack(i: Int): ItemStack = ItemStack.EMPTY

  override def takeInvStack(i: Int, i1: Int): ItemStack = ItemStack.EMPTY

  override def removeInvStack(i: Int): ItemStack = ItemStack.EMPTY

  override def setInvStack(i: Int, itemStack: ItemStack): Unit = {}

  override def canPlayerUseInv(playerEntity: PlayerEntity): Boolean = true

  override def clearInv(): Unit = {}

  override def tick(): Unit = {
    if (world.isClient || !world.getBlockState(pos).get(StockpileProperties.IsOpen)) {
      return
    }

    world
        .getEntities(classOf[ItemEntity], new BoundingBox(pos.up()), EntityPredicates.VALID_ENTITY)
        .forEach(_.kill())
  }
}
