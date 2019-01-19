package notjoe.stockpile.block

import net.fabricmc.fabric.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.class_3965
import net.minecraft.entity.VerticalEntityPosition
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.{SoundCategory, SoundEvents}
import net.minecraft.state.StateFactory
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.{BlockView, World}
import notjoe.stockpile.block.StockpileProperties.IsOpen
import notjoe.stockpile.blockentity.TrashCanBlockEntity

object TrashCanBlock extends BlockWithEntity(FabricBlockSettings.copy(Blocks.PISTON).build()) with Description {
  override def createBlockEntity(blockView: BlockView): BlockEntity = new TrashCanBlockEntity()

  override def appendProperties(builder: StateFactory.Builder[Block, BlockState]): Unit = {
    super.appendProperties(builder)
    builder.`with`(IsOpen)
  }

  override def getPlacementState(context: ItemPlacementContext): BlockState = {
    super.getPlacementState(context).`with`(IsOpen, boolean2Boolean(false)) // scala pls
  }

  override def getRenderType(blockState_1: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def activate(state: BlockState,
                        world: World,
                        pos: BlockPos,
                        player: PlayerEntity,
                        hand: Hand,
                        hitContext: class_3965): Boolean = {
    if (!world.isClient) {
      world.setBlockState(pos, state.`with`(IsOpen, boolean2Boolean(!state.get(IsOpen))), 3)
      world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCK, 0.5f, 0.8f)
    }

    true
  }

  override def isSimpleFullBlock(state: BlockState, view: BlockView, pos: BlockPos): Boolean = false

  override def canCollideWith(state: BlockState,
                              view: BlockView,
                              pos: BlockPos,
                              entityPosition: VerticalEntityPosition): VoxelShape =
    Block.createCubeShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)
}
