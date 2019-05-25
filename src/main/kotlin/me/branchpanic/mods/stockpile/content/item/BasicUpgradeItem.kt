package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.World

class BasicUpgradeItem(val upgradeSupplier: (ItemStack) -> Upgrade, settings: Settings) : Item(settings), UpgradeItem {
    override fun getUpgrade(stack: ItemStack): Upgrade = upgradeSupplier(stack)

    override fun getTranslatedNameTrimmed(stack: ItemStack?): Component {
        return super.getTranslatedNameTrimmed(stack).setStyle(UpgradeRegistry.UPGRADE_HEADER_STYLE)
    }

    override fun buildTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Component>?,
        context: TooltipContext?
    ) {
        if (stack == null) {
            return
        }

        tooltip?.add(TranslatableComponent("ui.stockpile.upgrade_application"))
        tooltip?.add(upgradeSupplier(stack).description.setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))
    }
}