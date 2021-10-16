package me.branchpanic.mods.stockpile.block

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object AttackableBlockCallback : AttackBlockCallback {
    override fun interact(
        player: PlayerEntity?,
        world: World?,
        hand: Hand?,
        pos: BlockPos?,
        direction: Direction?
    ): ActionResult {
        if (player == null ||
            world == null ||
            hand == null ||
            pos == null ||
            direction == null ||
            player.isSpectator ||
            player.isCreative
        ) {
            return ActionResult.PASS
        }

        val block = (world.getBlockState(pos).block as? AttackableBlock) ?: return ActionResult.PASS
        return block.onBlockAttacked(player, world, hand, pos, direction)
    }
}