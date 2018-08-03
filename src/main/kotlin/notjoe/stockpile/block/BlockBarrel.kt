package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceFluidMode
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.tile.inventory.OUTPUT_SLOT_INDEX

class BlockBarrel :
        BlockDirectional(Block.Builder.create(Material.WOOD).hardnessAndResistance(3f, 14f)), ITileEntityProvider {

    init {
        defaultState = blockState.baseState.withProperty(FACING, EnumFacing.NORTH)
    }

    override fun addPropertiesToBuilder(stateBuilder: StateContainer.Builder<Block, IBlockState>?) {
        stateBuilder?.addProperties(FACING)
    }

    override fun hasTileEntity(): Boolean = true
    override fun getTileEntity(p0: IBlockReader?): TileEntity? = TileBarrel()

    override fun onLeftClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?) {
        if (world == null || player == null || state == null) {
            return
        }

        // TODO: Find out where the reach value is stored instead of hardcoding it.
        val rayTraceResult = player.rayTraceFromEyes(4.0)

        if (rayTraceResult == null || rayTraceResult.sideHit != getFacing(state)) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        val amountToExtract = if (player.isSneaking) tile.inventoryStackLimit else 1
        val extractedStack = tile.decrStackSize(OUTPUT_SLOT_INDEX, amountToExtract)

        if (!extractedStack.isEmpty) {
            player.addItemStackToInventory(extractedStack)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.5f, 0.5f)
            tile.markDirty()
        }
    }

    override fun onRightClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?,
                              hand: EnumHand?, face: EnumFacing?, x: Float, y: Float, z: Float): Boolean {
        if (world == null || player == null) {
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

fun getFacing(state: IBlockState): EnumFacing {
    return state.getValue(BlockDirectional.FACING)
}

// Adapted from https://github.com/MinecraftForge/MinecraftForge/blob/2786cd279cd8feb5965060da27f141d7c8ccf1e5/src/main/java/net/minecraftforge/common/ForgeHooks.java#L1073
fun EntityLivingBase.rayTraceFromEyes(distance: Double,
                                      fluidMode: RayTraceFluidMode = RayTraceFluidMode.NEVER): RayTraceResult? {
    val startPos = Vec3d(posX, posY + eyeHeight, posZ)
    val endPos = startPos.add(lookVec.scale(distance))
    return world.rayTraceBlocks(startPos, endPos, fluidMode)
}