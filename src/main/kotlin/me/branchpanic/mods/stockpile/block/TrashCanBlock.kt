package me.branchpanic.mods.stockpile.block

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.blockentity.TrashCanBlockEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.tag.FluidTags
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

val IS_OPEN: BooleanProperty = BooleanProperty.of("is_open")

object TrashCanBlock : Block(FabricBlockSettings.copy(Blocks.IRON_BLOCK).build()), BlockEntityProvider, Waterloggable {
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(IS_OPEN, Properties.WATERLOGGED)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? {
        val fluid = context?.world?.getFluidState(context.blockPos)
            ?: throw NullPointerException("Received null item placement context!")

        return super.getPlacementState(context)?.with(IS_OPEN, false)
            ?.with(Properties.WATERLOGGED, fluid.isIn(FluidTags.WATER) && fluid.level == 8)
    }

    override fun canPathfindThrough(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        type: NavigationType?
    ): Boolean = false

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)

    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (world != null && state != null && !world.isClient) {
            world.setBlockState(pos, state.with(IS_OPEN, !state[IS_OPEN]), 3)
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f)
        }

        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(blockPos: BlockPos?, blockState: BlockState?): BlockEntity = TrashCanBlockEntity(blockPos, blockState)

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        blockState: BlockState?,
        blockEntityType: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return when (blockEntityType) {
            TrashCanBlockEntity.TYPE -> BlockEntityTicker<T> { world, pos, state, entity ->
                TrashCanBlockEntity.tick<T>(world,
                    pos,
                    state,
                    entity)
            }
            else -> {
                null
            }
        }
    }


    override fun getStateForNeighborUpdate(
        state: BlockState?,
        side: Direction?,
        neighborState: BlockState?,
        world: WorldAccess?,
        pos: BlockPos?,
        neighborPos: BlockPos?
    ): BlockState? {
        if (state?.get(Properties.WATERLOGGED) == true) {
            world?.fluidTickScheduler?.schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }

        return super.getStateForNeighborUpdate(
            state,
            side,
            neighborState,
            world,
            pos,
            neighborPos
        )
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        tooltip?.add(TranslatableText("block.stockpile.trash_can.desc").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }

    override fun getFluidState(state: BlockState): FluidState {
        @Suppress("DEPRECATION")
        return if (state[Properties.WATERLOGGED] == true) {
            Fluids.WATER.getStill(false)
        } else {
            super.getFluidState(state)
        }
    }
}
