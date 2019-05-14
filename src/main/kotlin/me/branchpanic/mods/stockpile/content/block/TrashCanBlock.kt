package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.content.blockentity.TrashCanBlockEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateFactory
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.loot.context.LootContext

val IS_OPEN: BooleanProperty = BooleanProperty.create("is_open")

object TrashCanBlock : Block(FabricBlockSettings.copy(Blocks.IRON_BLOCK).build()), BlockEntityProvider, Waterloggable {
    override fun appendProperties(builder: StateFactory.Builder<Block, BlockState>?) {
        if (builder == null) {
            return
        }

        builder.add(IS_OPEN, Properties.WATERLOGGED)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? {
        if (context == null) {
            return null
        }

        val fluid = context.world.getFluidState(context.blockPos)

        return super.getPlacementState(context)?.with(IS_OPEN, false)
            ?.with(Properties.WATERLOGGED, fluid.matches(FluidTags.WATER) && fluid.level == 8)
    }

    override fun isSimpleFullBlock(state: BlockState?, world: BlockView?, pos: BlockPos?): Boolean = false

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: EntityContext
    ): VoxelShape = createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)

    override fun getDroppedStacks(state: BlockState?, context: LootContext.Builder?): MutableList<ItemStack> {
        return mutableListOf(ItemStack(asItem()))
    }

    override fun onBlockRemoved(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        newState: BlockState?,
        unknown: Boolean
    ) {
        world?.removeBlockEntity(pos)
    }

    override fun activate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): Boolean {
        if (world != null && state != null && !world.isClient) {
            world.setBlockState(pos, state.with(IS_OPEN, !state[IS_OPEN]), 3)
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 0.8f)
        }

        return true
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? = TrashCanBlockEntity()

    override fun getStateForNeighborUpdate(
        state: BlockState?,
        side: Direction?,
        neighborState: BlockState?,
        world: IWorld?,
        pos: BlockPos?,
        neighborPos: BlockPos?
    ): BlockState {
        if (state?.get<Boolean>(Properties.WATERLOGGED) as Boolean) {
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

    override fun buildTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Component>?,
        context: TooltipContext?
    ) {
        tooltip?.add(TranslatableComponent("block.stockpile.trash_can.desc").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
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