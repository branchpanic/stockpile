package notjoe.stockpile.block

import net.minecraft.block.{Block, BlockState}
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateFactory
import net.minecraft.state.property.Properties
import net.minecraft.util.{Mirror, Rotation}

trait FacingDirection extends Block {
  abstract override def appendProperties(builder: StateFactory.Builder[Block, BlockState]): Unit = {
    super.appendProperties(builder)
    builder.`with`(Properties.FACING)
  }

  override def getPlacementState(context: ItemPlacementContext): BlockState = {
    super.getPlacementState(context).`with`(Properties.FACING, context.getPlayerFacing.getOpposite)
  }

  override def rotate(state: BlockState, rotation: Rotation): BlockState = {
    state.`with`(Properties.FACING, rotation.rotate(state.get(Properties.FACING)))
  }

  override def mirror(state: BlockState, mirror: Mirror): BlockState = {
    state.`with`(Properties.FACING, mirror.apply(state.get(Properties.FACING)))
  }
}
