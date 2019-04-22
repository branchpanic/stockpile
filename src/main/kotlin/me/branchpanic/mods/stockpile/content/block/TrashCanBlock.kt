package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.content.blockentity.TrashCanBlockEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.VerticalEntityPosition
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateFactory
import net.minecraft.state.property.BooleanProperty
import net.minecraft.text.TextComponent
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.loot.context.LootContext

val IS_OPEN: BooleanProperty = BooleanProperty.create("is_open")

object TrashCanBlock : Block(FabricBlockSettings.copy(Blocks.IRON_BLOCK).build()), BlockEntityProvider {
    override fun appendProperties(builder: StateFactory.Builder<Block, BlockState>?) {
        if (builder == null) {
            return
        }

        builder.with(IS_OPEN)
    }

    override fun getPlacementState(context: ItemPlacementContext?): BlockState? =
        super.getPlacementState(context)?.with(IS_OPEN, false)

    override fun isSimpleFullBlock(state: BlockState?, world: BlockView?, pos: BlockPos?): Boolean = false

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        verticalEntityPos: VerticalEntityPosition?
    ): VoxelShape = createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)

    override fun getDroppedStacks(state: BlockState?, context: LootContext.Builder?): MutableList<ItemStack> {
        return mutableListOf(ItemStack(item))
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

    override fun buildTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<TextComponent>?,
        context: TooltipContext?
    ) {
        tooltip?.add(TranslatableTextComponent("block.stockpile.trash_can.desc").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }
}