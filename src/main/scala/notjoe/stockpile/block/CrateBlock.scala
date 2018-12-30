package notjoe.stockpile.block

import java.util

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.{Block, BlockState, BlockWithEntity, Material}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.loot.context.{LootContext, Parameters}
import net.minecraft.world.{BlockView, World}
import notjoe.stockpile.blockentity.CrateBlockEntity

import scala.collection.JavaConverters._

object CrateBlock extends BlockWithEntity(Block.Settings.of(Material.WOOD).strength(2.5f, 2.0f)) with FacingDirection {
  final val STORED_DATA_TAG = "CrateData"

  override def createBlockEntity(blockView: BlockView): BlockEntity = new CrateBlockEntity()

  override def activate(state: BlockState,
                        world: World,
                        pos: BlockPos,
                        player: PlayerEntity,
                        hand: Hand,
                        direction: Direction,
                        hitX: Float,
                        hitY: Float,
                        hitZ: Float): Boolean = {
    if (!world.isClient) {
      world.getBlockEntity(pos)
        .asInstanceOf[CrateBlockEntity]
        .onRightClick(player)
    }

    true
  }

  override def getDroppedStacks(state: BlockState,
                                context: LootContext.Builder): util.List[ItemStack] = {
    val tag = context.get(Parameters.BLOCK_ENTITY)
      .asInstanceOf[CrateBlockEntity]
      .persistentDataToTag()

    val stack = new ItemStack(this, 1)
    stack.setChildTag(STORED_DATA_TAG, tag)

    List(stack).asJava
  }

  override def onPlaced(world: World,
                        pos: BlockPos,
                        state: BlockState,
                        placer: LivingEntity,
                        stack: ItemStack): Unit = {
    world.getBlockEntity(pos)
      .asInstanceOf[CrateBlockEntity]
      .loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(STORED_DATA_TAG))
  }

  override def onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
    world.getBlockEntity(pos)
      .asInstanceOf[CrateBlockEntity]
      .onLeftClick(player)
  }
}
