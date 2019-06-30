package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.giveTo
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.util.ActionResult
import net.minecraft.world.World

object UpgradeRemoverItem : Item(Stockpile.ITEM_SETTINGS) {

    override fun useOnBlock(ctx: ItemUsageContext?): ActionResult {
        if (ctx == null) {
            return ActionResult.FAIL
        }

        val world = ctx.world
        val blockEntity = world.getBlockEntity(ctx.blockPos) ?: return ActionResult.FAIL

        if (blockEntity !is UpgradeContainer || blockEntity.appliedUpgrades.isEmpty()) {
            return ActionResult.FAIL
        }

        val upgradeToRemove = blockEntity.appliedUpgrades.last()

        if (!upgradeToRemove.canSafelyBeRemovedFrom(blockEntity)) {
            return ActionResult.FAIL
        }

        blockEntity.popUpgrade()

        if (ctx.player != null) {
            upgradeToRemove.getCorrespondingStack().giveTo(ctx.player!!, playSound = false)
        }

        return ActionResult.SUCCESS
    }

    override fun buildTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Component>?,
        context: TooltipContext?
    ) {
        tooltip?.add(TranslatableComponent("item.stockpile.upgrade_remover.desc").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }
}
