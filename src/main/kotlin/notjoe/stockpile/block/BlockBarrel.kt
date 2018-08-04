package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.util.rayTraceFromEyes

class BlockBarrel(private val maxStacks: Int = 32) :
        BlockDirectional(Block.Builder.create(Material.WOOD).hardnessAndResistance(3f, 14f)), ITileEntityProvider {

    init {
        defaultState = blockState.baseState.withProperty(FACING, EnumFacing.NORTH)
    }

    override fun addPropertiesToBuilder(stateBuilder: StateContainer.Builder<Block, IBlockState>?) {
        stateBuilder?.addProperties(FACING)
    }

    override fun hasTileEntity(): Boolean = true
    override fun getTileEntity(p0: IBlockReader?): TileEntity? = TileBarrel(maxStacks)

    override fun onLeftClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?) {
        if (world == null || player == null || state == null || world.isRemote) {
            return
        }

        val rayTraceResult = player.rayTraceFromEyes(4.0)

        if (rayTraceResult == null || rayTraceResult.sideHit != state.getValue(FACING)) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleLeftClick(player)
    }

    override fun onRightClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?,
                              hand: EnumHand?, face: EnumFacing?, x: Float, y: Float, z: Float): Boolean {
        if (world == null || player == null || world.isRemote) {
            return true
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleRightClick(player)

        return true
    }

    override fun getBlockToPlaceOnUse(context: BlockItemUseContext?): IBlockState? {
        if (context == null) {
            return null
        }

        return defaultState.withProperty(FACING, context.func_196010_d())
    }
}