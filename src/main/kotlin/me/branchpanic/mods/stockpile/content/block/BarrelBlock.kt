package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.item.UpgradeRemoverItem
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

open class BarrelBlock<T : AbstractBarrelBlockEntity<*>>(
    private val supplier: (BlockPos, BlockState) -> T
) :
    Block(Settings.copy(Blocks.CHEST)),
    BlockEntityProvider, AttackableBlock {

    companion object {
        val ITEM = ItemBarrelBlock
    }

    private val contentsTooltipStyle = Style.EMPTY.withColor(Formatting.GRAY)

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

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity? = supplier.invoke(blockPos, blockState)

    override fun getDroppedStacks(state: BlockState?, context: LootContext.Builder?): MutableList<ItemStack> {
        if (context == null) {
            return mutableListOf()
        }

        val barrel = context[LootContextParameters.BLOCK_ENTITY] as? T ?: return mutableListOf()
        val stack = ItemStack(this)

        if (barrel.storage.isEmpty) {
            barrel.clearWhenEmpty = true
        }

        stack.setSubNbt(ItemBarrelBlockEntity.STORED_BLOCK_ENTITY_TAG, barrel.toClientTag(NbtCompound()))
        return mutableListOf(stack)
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

        (world.getBlockEntity(pos) as? T)?.onLeftClicked(player)

        return ActionResult.PASS
    }

    override fun onStateReplaced(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        newState: BlockState?,
        moved: Boolean
    ) {
        world?.removeBlockEntity(pos)  // TODO(1.16): Still needed?
    }

    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (world == null ||
            player == null ||
            pos == null ||
            hit == null ||
            state == null ||
            hand != Hand.MAIN_HAND ||
            hit.side != state[Properties.FACING]
        ) {
            return ActionResult.FAIL
        }

        if (player.getStackInHand(Hand.MAIN_HAND).item == UpgradeRemoverItem) {
            return ActionResult.FAIL
        }

        if (!world.isClient) {
            (world.getBlockEntity(pos) as? T)?.onRightClicked(player)
        }

        return ActionResult.SUCCESS
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = true

    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?): Int {
        val barrel = (world?.getBlockEntity(pos) as? AbstractBarrelBlockEntity<*>) ?: return 0
        val amountStored = barrel.storage.contents.amount

        return if (amountStored <= 0) {
            0
        } else {
            1 + (14.0 * amountStored / (barrel.storage.capacity)).toInt()
        }
    }

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        lines: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, lines, context)

        if (stack == null || lines == null) return

        val barrel = supplier.invoke(BlockPos.ORIGIN, ITEM.defaultState)
        barrel.readNbt(stack.getOrCreateSubNbt(ItemBarrelBlockEntity.STORED_BLOCK_ENTITY_TAG))
        lines.add(barrel.storage.describeContents().shallowCopy().setStyle(contentsTooltipStyle))

        if (barrel is UpgradeContainer) lines.addAll(UpgradeRegistry.createTooltip(barrel))
    }

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?): Boolean = true

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

        (world.getBlockEntity(pos) as? T)?.apply {
            fromClientTag(stack.getOrCreateSubNbt(ItemBarrelBlockEntity.STORED_BLOCK_ENTITY_TAG))
            markDirty()
        }
    }
}
