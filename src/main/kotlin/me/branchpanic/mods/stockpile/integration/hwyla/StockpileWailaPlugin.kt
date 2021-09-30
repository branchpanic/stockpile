package me.branchpanic.mods.stockpile.integration.hwyla

import mcp.mobius.waila.api.*
import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

object BarrelInfoComponent : IComponentProvider {
    override fun appendBody(tooltip: MutableList<Text>?, accessor: IDataAccessor?, config: IPluginConfig?) {
        tooltip?.add((accessor?.getBlockEntity() as? AbstractBarrelBlockEntity<*>)?.storage?.describeContents() ?: return)
    }
}

object UpgradeInfoComponent : IComponentProvider {
    override fun appendBody(tooltip: MutableList<Text>?, accessor: IDataAccessor?, config: IPluginConfig?) {
        val blockEntity = accessor?.getBlockEntity() as? UpgradeContainer ?: return
        tooltip?.add(
            TranslatableText(
                "ui.stockpile.applied_upgrades",
                blockEntity.appliedUpgrades.count(),
                blockEntity.maxUpgrades
            )
        )
    }
}

class StockpileWailaPlugin : IWailaPlugin {
    override fun register(wailaRegistrar: IRegistrar?) {
        requireNotNull(wailaRegistrar)

        wailaRegistrar.registerComponentProvider(
            BarrelInfoComponent,
            TooltipPosition.BODY,
            ItemBarrelBlockEntity::class.java
        )

        wailaRegistrar.registerComponentProvider(
            UpgradeInfoComponent,
            TooltipPosition.BODY,
            ItemBarrelBlockEntity::class.java
        )
    }
}
