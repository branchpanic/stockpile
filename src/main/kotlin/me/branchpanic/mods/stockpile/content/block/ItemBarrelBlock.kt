package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.state.StateFactory
import net.minecraft.state.property.Properties
import net.minecraft.text.Style
import net.minecraft.text.TextComponent
import net.minecraft.text.TextFormat
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.Hand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.RayTraceContext
import net.minecraft.world.World
import net.minecraft.world.loot.context.LootContext
import net.minecraft.world.loot.context.LootContextParameters
import java.text.NumberFormat

object ItemBarrelBlock : Block(FabricBlockSettings.copy(Blocks.CHEST).build()), BlockEntityProvider {
    private const val STORED_BLOCK_ENTITY_TAG = "StoredBlockEntity"
    private const val PLAYER_REACH = 5

    private val CONTENTS_STYLE = Style().setColor(TextFormat.GRAY)

    override fun appendProperties(builder: StateFactory.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.with(Properties.FACING)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? =
        super.getPlacementState(context)
            ?.with(Properties.FACING, context?.playerFacing?.opposite ?: Direction.NORTH)

    override fun rotate(state: BlockState?, rotation: Rotation?): BlockState =
        state?.with(Properties.FACING, rotation?.rotate(state.get(Properties.FACING)) ?: Direction.NORTH)
            ?: throw NullPointerException("attempted to rotate null item barrel")

    override fun mirror(state: BlockState?, mirror: Mirror?): BlockState =
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
            fromTagWithoutWorldInfo(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
            markDirty()
        }
    }

    override fun onBlockBreakStart(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?
    ) {
        if (world == null || player == null || pos == null || world.isClient || player.activeHand != Hand.MAIN) {
            return
        }

        val rayTraceStart = player.getCameraPosVec(1f)
        val rayTraceEnd = rayTraceStart.add(player.getRotationVec(1f).multiply(PLAYER_REACH.toDouble()))
        val result = world.rayTrace(
            RayTraceContext(
                rayTraceStart,
                rayTraceEnd,
                RayTraceContext.ShapeType.OUTLINE,
                RayTraceContext.FluidHandling.NONE,
                player
            )
        )

        if (result.type != HitResult.Type.BLOCK || result.side != state?.get(Properties.FACING)) {
            return
        }

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).onPunched(player)
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
            hand != Hand.MAIN ||
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

        if (!barrel.backingStorage.isEmpty) {
            stack.setChildTag(STORED_BLOCK_ENTITY_TAG, barrel.toTagWithoutWorldInfo(CompoundTag()))
        }

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

    override fun getRenderLayer(): BlockRenderLayer = BlockRenderLayer.MIPPED_CUTOUT

    override fun buildTooltip(
        stack: ItemStack?,
        world: BlockView?,
        lines: MutableList<TextComponent>?,
        context: TooltipContext?
    ) {
        super.buildTooltip(stack, world, lines, context)

        if (stack == null || lines == null) return

        val barrel = ItemBarrelBlockEntity(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))

        if (barrel.backingStorage.isEmpty) {
            lines.add(TranslatableTextComponent("ui.stockpile.empty").setStyle(CONTENTS_STYLE))
            return
        }

        val f = NumberFormat.getInstance()
        lines.add(
            TranslatableTextComponent(
                "ui.stockpile.barrel.contents_stack",
                barrel.backingStorage.currentInstance.displayName.formattedText,
                f.format(barrel.backingStorage.amountStored),
                f.format(barrel.backingStorage.amountStored / barrel.backingStorage.currentInstance.maxAmount)
            ).setStyle(CONTENTS_STYLE)
        )

        if (barrel.upgrades.isNotEmpty()) {
            lines.add(TranslatableTextComponent("ui.stockpile.barrel.active_upgrades"))
            lines.addAll(barrel.upgrades.map { u -> u.description })
        }
    }
}