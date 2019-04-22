package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.content.block.IS_OPEN
import me.branchpanic.mods.stockpile.content.block.TrashCanBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.predicate.entity.EntityPredicates
import net.minecraft.util.Tickable
import net.minecraft.util.math.BoundingBox
import net.minecraft.util.math.Direction

class TrashCanBlockEntity : BlockEntity(TYPE), SidedInventory, Tickable {
    companion object {
        val TYPE: BlockEntityType<TrashCanBlockEntity> = BlockEntityType({ TrashCanBlockEntity() }, null)
    }

    override fun tick() {
        if (world == null || world?.isClient != false) {
            return
        }

        val currentState = world?.getBlockState(pos) ?: return

        if (currentState.block != TrashCanBlock || !currentState[IS_OPEN]) {
            return
        }

        world?.getEntities(EntityType.ITEM, BoundingBox(pos.up()), EntityPredicates.VALID_ENTITY)
            ?.forEach { e -> e.kill() }
    }

    override fun getInvStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun markDirty() = Unit

    override fun clear() = Unit

    override fun setInvStack(slot: Int, stack: ItemStack?) = Unit

    override fun removeInvStack(slot: Int): ItemStack = ItemStack.EMPTY

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean = true

    override fun getInvAvailableSlots(side: Direction?): IntArray = intArrayOf(0)

    override fun getInvSize(): Int = 1

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, side: Direction?): Boolean = true

    override fun takeInvStack(slot: Int, amount: Int): ItemStack = ItemStack.EMPTY

    override fun isInvEmpty(): Boolean = false

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, side: Direction?): Boolean = true
}