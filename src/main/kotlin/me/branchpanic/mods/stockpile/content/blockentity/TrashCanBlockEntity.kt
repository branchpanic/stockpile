package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.content.block.IS_OPEN
import me.branchpanic.mods.stockpile.content.block.TrashCanBlock
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class TrashCanBlockEntity(blockPos: BlockPos?, blockState: BlockState?) : BlockEntity(TYPE, blockPos, blockState), SidedInventory {
    companion object {
        val TYPE: BlockEntityType<TrashCanBlockEntity> =
            BlockEntityType.Builder.create({ blockPos: BlockPos, blockState: BlockState -> TrashCanBlockEntity(blockPos, blockState) }, TrashCanBlock).build(null)

            fun <T: BlockEntity?> tick(world: World, pos: BlockPos, state: BlockState, entity: BlockEntity?) {
                if (world.isClient) {
                    return
                }

                val currentState = world.getBlockState(pos) ?: return

                if (currentState.block != TrashCanBlock || !currentState[IS_OPEN]) {
                    return
                }

                world.getEntitiesByType(EntityType.ITEM, Box(pos.up()), EntityPredicates.VALID_ENTITY)
                    ?.forEach { e -> e.kill() }
            }
    }

    fun tick() {
        if (world == null || world?.isClient != false) {
            return
        }

        val currentState = world?.getBlockState(pos) ?: return

        if (currentState.block != TrashCanBlock || !currentState[IS_OPEN]) {
            return
        }

        world?.getEntitiesByType(EntityType.ITEM, Box(pos.up()), EntityPredicates.VALID_ENTITY)
            ?.forEach { e -> e.kill() }
    }

    override fun getStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun markDirty() = Unit

    override fun clear() = Unit

    override fun setStack(slot: Int, stack: ItemStack?) = Unit

    override fun removeStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun canPlayerUse(player: PlayerEntity?): Boolean = true

    override fun getAvailableSlots(side: Direction?): IntArray = intArrayOf(0)

    override fun size(): Int = 1

    override fun canExtract(slot: Int, stack: ItemStack?, side: Direction?): Boolean = true

    override fun removeStack(slot: Int, amount: Int): ItemStack = ItemStack.EMPTY

    override fun isEmpty(): Boolean = false

    override fun canInsert(slot: Int, stack: ItemStack?, side: Direction?): Boolean = true


}