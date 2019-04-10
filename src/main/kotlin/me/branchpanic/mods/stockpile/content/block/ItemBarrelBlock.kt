package me.branchpanic.mods.stockpile.content.block

import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

object ItemBarrelBlock : Block(FabricBlockSettings.copy(Blocks.CHEST).build()), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity? = ItemBarrelBlockEntity()

    override fun onBlockBreakStart(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?
    ) {
        if (world == null || player == null || pos == null || world.isClient) {
            return
        }

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).onPunched(player)
    }

    override fun activate(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): Boolean {
        if (world == null || player == null || pos == null || world.isClient) {
            return true
        }

        (world.getBlockEntity(pos) as ItemBarrelBlockEntity).onActivated(player)

        return true
    }
}