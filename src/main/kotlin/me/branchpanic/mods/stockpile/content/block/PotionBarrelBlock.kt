package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.content.blockentity.PotionBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.item.UpgradeRemoverItem
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateFactory
import net.minecraft.state.property.Properties
import net.minecraft.text.Style
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

object PotionBarrelBlock : Block(FabricBlockSettings.copy(Blocks.CHEST).build()), BlockEntityProvider, AttackableBlock {
    private val CONTENTS_STYLE = Style().setColor(Formatting.GRAY)

    override fun appendProperties(builder: StateFactory.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(Properties.FACING)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? =
        super.getPlacementState(context)
            ?.with(Properties.FACING, context?.playerFacing?.opposite ?: Direction.NORTH)

    override fun rotate(state: BlockState?, rotation: BlockRotation?): BlockState =
        state?.with(Properties.FACING, rotation?.rotate(state.get(Properties.FACING)) ?: Direction.NORTH)
            ?: throw NullPointerException("attempted to rotate null item barrel")

    override fun mirror(state: BlockState?, mirror: BlockMirror?): BlockState =
        state?.with(Properties.FACING, mirror?.apply(state.get(Properties.FACING)) ?: Direction.NORTH)
            ?: throw NullPointerException("attempted to mirror null item barrel")

    override fun createBlockEntity(world: BlockView?): BlockEntity? = PotionBarrelBlockEntity()

    override fun onBlockAttacked(
        player: PlayerEntity,
        world: World,
        hand: Hand,
        pos: BlockPos,
        direction: Direction
    ): ActionResult {
        if (world.isClient) {
            return ActionResult.PASS
        }

        val state = world.getBlockState(pos) ?: return ActionResult.PASS

        if (direction != state.get(Properties.FACING)) {
            return ActionResult.PASS
        }

        (world.getBlockEntity(pos) as PotionBarrelBlockEntity).onLeftClicked(player)

        return ActionResult.PASS
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
        if (world == null ||
            player == null ||
            pos == null ||
            hit == null ||
            state == null ||
            hand != Hand.MAIN_HAND ||
            hit.side != state[Properties.FACING]
        ) {
            return false
        }

        if (player.getStackInHand(Hand.MAIN_HAND).item == UpgradeRemoverItem) {
            return false
        }

        if (world.isClient) {
            return true
        }

        (world.getBlockEntity(pos) as PotionBarrelBlockEntity).onRightClicked(player)

        return true
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = true

    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int {
        val barrel = (world?.getBlockEntity(pos) as? PotionBarrelBlockEntity) ?: return 0
        val amountStored = barrel.storage.contents.amount

        return if (amountStored <= 0) {
            0
        } else {
            1 + (14.0 * amountStored / (barrel.storage.capacity)).toInt()
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun getRenderLayer(): BlockRenderLayer = BlockRenderLayer.CUTOUT_MIPPED
}