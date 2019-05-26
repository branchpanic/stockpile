package me.branchpanic.mods.stockpile.content.block

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.ChatFormat
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.state.StateFactory
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
import net.minecraft.world.loot.context.LootContext
import net.minecraft.world.loot.context.LootContextParameters

object ItemBarrelBlock : Block(FabricBlockSettings.copy(Blocks.CHEST).build()), BlockEntityProvider, AttackableBlock,
    AttributeProvider {
    private val CONTENTS_STYLE = Style().setColor(ChatFormat.GRAY)

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

    override fun createBlockEntity(world: BlockView?): BlockEntity? = ItemBarrelBlockEntity()

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        stack: ItemStack?
    ) {
        if (world == null || stack == null || world.isClient) {
            return
        }

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).apply {
            fromStack(stack)
            markDirty()
        }
    }

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

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).onPunched(player)

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

        if (world.isClient) {
            return true
        }

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).onActivated(player)

        return true
    }

    override fun getDroppedStacks(state: BlockState?, context: LootContext.Builder?): MutableList<ItemStack> {
        if (context == null) {
            return mutableListOf()
        }

        val barrel = context[LootContextParameters.BLOCK_ENTITY] as? ItemBarrelBlockEntity ?: return mutableListOf()
        val stack = ItemStack(this)

        if (barrel.backingStorage.isEmpty) {
            barrel.backingStorage.clearInstanceWhenEmpty()
        }

        barrel.toStack(stack)

        return mutableListOf(stack)
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = true

    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int {
        val barrel = (world?.getBlockEntity(pos) as? ItemBarrelBlockEntity) ?: return 0
        val amountStored = barrel.backingStorage.amountStored

        return if (amountStored <= 0) {
            0
        } else {
            1 + (14.0 * amountStored / (barrel.backingStorage.capacity)).toInt()
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun getRenderLayer(): BlockRenderLayer = BlockRenderLayer.CUTOUT_MIPPED

    override fun buildTooltip(
        stack: ItemStack?,
        world: BlockView?,
        lines: MutableList<Component>?,
        context: TooltipContext?
    ) {
        super.buildTooltip(stack, world, lines, context)

        if (stack == null || lines == null) return

        val barrel = ItemBarrelBlockEntity.loadFromStack(stack)

        lines.add(barrel.getContentDescription().setStyle(CONTENTS_STYLE))
        lines.addAll(UpgradeRegistry.createTooltip(barrel))
    }

    override fun addAllAttributes(world: World?, pos: BlockPos?, state: BlockState?, attributes: AttributeList<*>?) {
        if (world == null || pos == null || state == null || attributes == null) {
            return
        }

        (world.getBlockEntity(pos) as? ItemBarrelBlockEntity)?.let { b -> attributes.offer(b.invAttribute) }
    }
}