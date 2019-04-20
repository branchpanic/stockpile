package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.TextComponent
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.world.World

class BasicUpgradeItem(val upgradeSupplier: (ItemStack) -> Upgrade, settings: Settings) : Item(settings), UpgradeItem {
    override fun getUpgrade(stack: ItemStack): Upgrade = upgradeSupplier(stack)

    override fun getTranslatedNameTrimmed(stack: ItemStack?): TextComponent {
        return super.getTranslatedNameTrimmed(stack).setStyle(UpgradeRegistry.UPGRADE_HEADER_STYLE)
    }

    override fun buildTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<TextComponent>?,
        context: TooltipContext?
    ) {
        if (stack == null) {
            return
        }

        tooltip?.add(TranslatableTextComponent("ui.stockpile.upgrade_application"))
        tooltip?.add(upgradeSupplier(stack).description.setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }
}