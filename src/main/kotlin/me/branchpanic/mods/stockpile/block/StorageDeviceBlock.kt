package me.branchpanic.mods.stockpile.block

import me.branchpanic.mods.stockpile.api.StorageDeviceBlockEntity
import me.branchpanic.mods.stockpile.item.UpgradeRemoverItem
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.function.Supplier

open class StorageDeviceBlock<T : StorageDeviceBlockEntity>(private val typeSupplier: Supplier<BlockEntityType<T>>):
    BlockWithEntity(Settings.copy(Blocks.CHEST)), AttackableBlock {

    private val blockEntityType by lazy { typeSupplier.get() }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(Properties.FACING)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? =
        super.getPlacementState(context)
            ?.with(Properties.FACING, context?.playerFacing?.opposite ?: Direction.NORTH)

    override fun rotate(state: BlockState?, rotation: BlockRotation?): BlockState =
        state?.with(Properties.FACING, rotation?.rotate(state.get(Properties.FACING)) ?: Direction.NORTH)
            ?: throw NullPointerException("attempted to rotate null barrel")

    override fun mirror(state: BlockState?, mirror: BlockMirror?): BlockState =
        state?.with(Properties.FACING, mirror?.apply(state.get(Properties.FACING)) ?: Direction.NORTH)
            ?: throw NullPointerException("attempted to mirror null barrel")

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity? =
        blockEntityType.instantiate(blockPos, blockState)

    override fun onBlockAttacked(
        player: PlayerEntity,
        world: World,
        hand: Hand,
        pos: BlockPos,
        direction: Direction
    ): ActionResult {
        if (world.isClient) return ActionResult.PASS

        val state = world.getBlockState(pos) ?: return ActionResult.PASS
        if (direction != state.get(Properties.FACING)) return ActionResult.PASS

        val blockEntity = blockEntityType.get(world, pos) ?: return ActionResult.PASS
        blockEntity.onPrimaryInteraction(player)

        return ActionResult.PASS
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (hand != Hand.MAIN_HAND || hit.side != state[Properties.FACING]) return ActionResult.FAIL

        if (player.getStackInHand(Hand.MAIN_HAND).item == UpgradeRemoverItem) {
            return ActionResult.FAIL
        }

        if (!world.isClient) {
            val blockEntity = blockEntityType.get(world, pos) ?: return ActionResult.SUCCESS
            blockEntity.onSecondaryInteraction(player)
        }

        return ActionResult.SUCCESS
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL
}
