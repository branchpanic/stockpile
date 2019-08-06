package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.extension.giveTo
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
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
            upgradeToRemove.toStack().giveTo(ctx.player!!)
        }

        return ActionResult.SUCCESS
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        tooltip?.add(TranslatableText("item.stockpile.upgrade_remover.desc").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }
}
