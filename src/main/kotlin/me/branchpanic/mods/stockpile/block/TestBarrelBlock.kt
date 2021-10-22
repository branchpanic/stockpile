package me.branchpanic.mods.stockpile.block

import me.branchpanic.mods.stockpile.blockentity.ItemStorageDeviceBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object TestBarrelBlock : BlockWithEntity(FabricBlockSettings.copy(Blocks.IRON_BLOCK)), AttackableBlock {
    override fun createBlockEntity(blockPos: BlockPos?, blockState: BlockState?): BlockEntity? {
        return ItemStorageDeviceBlockEntity(blockPos, blockState)
    }

    override fun onBlockAttacked(
        player: PlayerEntity,
        world: World,
        hand: Hand,
        pos: BlockPos,
        direction: Direction
    ): ActionResult {
        if (world.isClient) return ActionResult.PASS

        world.getBlockEntity(pos, ItemStorageDeviceBlockEntity.TYPE)?.ifPresent {
            it.onPrimaryInteraction(player)
        }

        return ActionResult.PASS
    }
}