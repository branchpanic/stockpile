@file:Suppress("OverridingDeprecatedMember", "DEPRECATION")

package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.state.BooleanProperty
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import notjoe.stockpile.tile.TileTrashCan

class BlockTrashCan :
        Block(
                Block.Builder
                        .create(Material.ROCK)
                        .hardnessAndResistance(3f, 12f)
        ), ITileEntityProvider {

    companion object Properties {
        val LID_OPEN = BooleanProperty.create("lid_open")!!
    }

    init {
        defaultState = blockState.baseState
                .withProperty(LID_OPEN, false)
                .withProperty(BlockStateProperties.WATERLOGGED, false)
    }

    override fun hasTileEntity(): Boolean = true
    override fun createNewTileEntity(p0: IBlockReader?): TileEntity? = TileTrashCan()

    override fun addPropertiesToBuilder(builder: StateContainer.Builder<Block, IBlockState>?) {
        builder?.add(LID_OPEN, BlockStateProperties.WATERLOGGED)
    }

    override fun onReplaced(
            oldState: IBlockState?,
            world: World?,
            pos: BlockPos?,
            newState: IBlockState?,
            unknown: Boolean
    ) {
        super.onReplaced(oldState, world, pos, newState, unknown)

        if (oldState?.block != newState?.block && pos != null) {
            world?.removeTileEntity(pos)
        }
    }

    override fun isFullCube(state: IBlockState?): Boolean = false
    override fun getShape(state: IBlockState?, world: IBlockReader?, pos: BlockPos?): VoxelShape {
        return Block.makeCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)
    }

    override fun onBlockActivated(
            state: IBlockState?,
            world: World?,
            pos: BlockPos?,
            player: EntityPlayer?,
            hand: EnumHand?,
            face: EnumFacing?,
            hitX: Float,
            hitY: Float,
            hitZ: Float
    ): Boolean {
        if (state == null || world == null || pos == null || player == null || world.isRemote) {
            return true
        }

        world.setBlockState(pos, state.withProperty(LID_OPEN, !state.getValue(LID_OPEN)), 3)
        world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f)
        return true
    }
}