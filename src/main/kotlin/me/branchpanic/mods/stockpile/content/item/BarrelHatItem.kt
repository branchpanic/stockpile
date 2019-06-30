package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.StockpileClient
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.min

object BarrelHatMaterial : ArmorMaterial by ArmorMaterials.LEATHER {
    override fun getName(): String = "barrel_hat"
}

object BarrelHatItem : ArmorItem(BarrelHatMaterial, EquipmentSlot.HEAD, Stockpile.ITEM_SETTINGS) {
    private fun getInventoryStacks(player: PlayerEntity): List<ItemStack> {
        return (0 until player.inventory.invSize).map { i ->
            player.inventory.getInvStack(i)
        }
    }

    private fun getUsableBarrelStacks(stacks: List<ItemStack>): List<ItemStack> {
        return stacks.filter { s -> s.amount == 1 && s.item == Registry.ITEM[Stockpile.id("item_barrel")] }
    }

    fun pushInventoryToBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getUsableBarrelStacks(invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val insertableStacks = invStacks.filter { s -> s !in barrelStacks }
        var itemsDeposited = 0

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)

            insertableStacks.forEach insertStack@{ insertStack ->
                if (!barrel.backingStorage.accepts(insertStack)) {
                    return@insertStack
                }

                val amount = insertStack.amount

                if (amount <= 1) {
                    return@insertStack
                }

                val maxInsertableAmount = amount - 1
                val remainder = (barrel.backingStorage.add(maxInsertableAmount.toLong()) + 1).toInt()
                insertStack.amount = remainder
                itemsDeposited += amount - remainder
            }

            barrel.toStack(barrelStack)
        }

        if (itemsDeposited > 0) {
            player.addChatMessage(TranslatableComponent("ui.stockpile.barrel_hat.pushed_items", itemsDeposited), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 0.9f)
        } else {
            player.addChatMessage(TranslatableComponent("ui.stockpile.barrel_hat.no_pushed_items"), true)
            player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 0.5f, 1.0f)
        }
        player.inventory.markDirty()
    }

    fun pullInventoryFromBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getUsableBarrelStacks(invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val restockableStacks = invStacks.filter { s -> s !in barrelStacks && s.item !is UpgradeItem }
        var itemsTaken = 0

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)


            restockableStacks.forEach restockStack@{ restockStack ->
                if (!barrel.backingStorage.accepts(restockStack)) {
                    return@restockStack
                }

                val amountNeededForFullStack = restockStack.maxAmount - restockStack.amount
                val removableAmount = barrel.backingStorage.remove(
                    min(
                        amountNeededForFullStack.toLong(),
                        barrel.backingStorage.amountStored - 1
                    )
                ).toInt()

                restockStack.amount += removableAmount
                itemsTaken += removableAmount
            }

            barrel.toStack(barrelStack)
        }

        if (itemsTaken > 0) {
            player.addChatMessage(TranslatableComponent("ui.stockpile.barrel_hat.pulled_items", itemsTaken), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 1.1f)
        } else {
            player.addChatMessage(TranslatableComponent("ui.stockpile.barrel_hat.no_pulled_items"), true)
            player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 0.5f, 1.0f)
        }

        player.inventory.markDirty()
    }

    override fun buildTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Component>?,
        context: TooltipContext?
    ) {
        val keyName = StockpileClient.BARREL_HAT_KEY.localizedName.toUpperCase()

        tooltip?.add(TranslatableComponent("ui.stockpile.barrel_hat").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))

        tooltip?.add(
            TranslatableComponent(
                "ui.stockpile.barrel_hat_push",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )

        tooltip?.add(
            TranslatableComponent(
                "ui.stockpile.barrel_hat_pull",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )
    }
}