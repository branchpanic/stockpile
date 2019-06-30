package me.branchpanic.mods.stockpile.content.upgrade

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

object UpgradeInstallerCallback : UseBlockCallback {
    override fun interact(player: PlayerEntity?, world: World?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        if (player == null || world == null || hand == null || hit == null || player.isSpectator) {
            return ActionResult.PASS
        }

        val heldItem = player.getStackInHand(hand)

        val upgrade = (heldItem.item as? UpgradeItem)?.getUpgrade(heldItem) ?: return ActionResult.PASS

        val blockEntity = (world.getBlockEntity(hit.blockPos) as? UpgradeContainer) ?: return ActionResult.PASS

        if (blockEntity.appliedUpgrades.size >= blockEntity.maxUpgrades) {
            return ActionResult.FAIL
        }

        if (!blockEntity.isUpgradeTypeAllowed(upgrade)) {
            return ActionResult.FAIL
        }

        val conflicts = upgrade.getConflictingUpgrades(blockEntity.appliedUpgrades)

        if (conflicts.isNotEmpty()) {
            return ActionResult.FAIL
        }

        if (world.isClient) {
            return ActionResult.SUCCESS
        }

        blockEntity.pushUpgrade(upgrade)
        heldItem.subtractAmount(1)
        player.inventory.markDirty()

        player.addChatMessage(TranslatableComponent("ui.stockpile.upgrade_applied"), true)

        return ActionResult.SUCCESS
    }
}