package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.StockpileClient
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TextComponent
import net.minecraft.text.TranslatableTextComponent
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object BarrelHatMaterial : ArmorMaterial by ArmorMaterials.LEATHER {
    override fun getName(): String = "barrel_hat"
}

object BarrelHatItem : ArmorItem(BarrelHatMaterial, EquipmentSlot.HEAD, Stockpile.ITEM_SETTINGS) {
    private fun getInventoryStacks(player: PlayerEntity): List<ItemStack> {
        return (0 until player.inventory.invSize).map { i ->
            player.inventory.getInvStack(i)
        }
    }

    private fun getBarrelStacks(stacks: List<ItemStack>): List<ItemStack> {
        return stacks.filter { s -> s.item == Registry.ITEM[Stockpile.id("item_barrel")] }
    }

    fun pushInventoryToBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getBarrelStacks(invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val insertableStacks = invStacks.filter { s -> s !in barrelStacks && s.item !is UpgradeItem }

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)

            insertableStacks.forEach { insertStack ->
                val remainder = barrel.backingStorage.offer(insertStack)
                insertStack.amount = remainder?.amount ?: 0
            }

            barrel.toStack(barrelStack)
        }

        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 1.0f)
        player.inventory.markDirty()
    }

    fun pullInventoryFromBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getBarrelStacks(invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val restockableStacks = invStacks.filter { s -> s !in barrelStacks && s.item !is UpgradeItem }

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)

            restockableStacks.forEach { restockStack ->
                if (barrel.backingStorage.accepts(restockStack)) {
                    val amountNeededForFullStack = restockStack.maxAmount - restockStack.amount
                    val removableAmount = barrel.backingStorage.remove(amountNeededForFullStack.toLong()).toInt()
                    restockStack.amount += removableAmount
                }
            }

            barrel.toStack(barrelStack)
        }

        player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 1.0f)
        player.inventory.markDirty()
    }

    override fun buildTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<TextComponent>?,
        context: TooltipContext?
    ) {
        val keyName = StockpileClient.BARREL_HAT_KEY.localizedName.toUpperCase()

        tooltip?.add(TranslatableTextComponent("ui.stockpile.barrel_hat").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))

        tooltip?.add(
            TranslatableTextComponent(
                "ui.stockpile.barrel_hat_push",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )

        tooltip?.add(
            TranslatableTextComponent(
                "ui.stockpile.barrel_hat_pull",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )
    }
}