package me.branchpanic.mods.stockpile.content.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.Stockpile.id
import me.branchpanic.mods.stockpile.StockpileClient
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.withCount
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
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

    private fun getUsableBarrelStacks(player: PlayerEntity, stacks: List<ItemStack>): List<ItemStack> {
        val potentialStacks = stacks.filter { s -> s.item == Registry.ITEM[id("item_barrel")] }

        if (potentialStacks.any { s -> s.count > 1 }) {
            player.addChatMessage(
                TranslatableText("ui.stockpile.barrel_hat.stacked_warning").setStyle(
                    Style().setColor(
                        Formatting.GRAY
                    )
                ), false
            )
        }

        return potentialStacks.filter { s -> s.count == 1 }
    }

    fun pushInventoryToBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getUsableBarrelStacks(player, invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val insertableStacks = invStacks.filter { s -> s !in barrelStacks }
        var itemsDeposited = 0

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)

            insertableStacks.forEach insertStack@{ insertStack ->
                if (!barrel.backingStorage.accepts(insertStack) || insertStack.isEmpty) {
                    return@insertStack
                }

                val amount = insertStack.count

                if (amount <= 1) {
                    return@insertStack
                }

                val maxInsertableStack = insertStack.withCount(amount - 1)
                val remainder = barrel.backingStorage.offer(maxInsertableStack).count + 1

                insertStack.count = remainder
                itemsDeposited += amount - remainder
            }

            barrel.toStack(barrelStack)
        }

        if (itemsDeposited > 0) {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel_hat.pushed_items", itemsDeposited), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 0.9f)
        } else {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel_hat.no_pushed_items"), true)
            player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 0.5f, 1.0f)
        }
        player.inventory.markDirty()
    }

    fun pullInventoryFromBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getUsableBarrelStacks(player, invStacks)

        if (barrelStacks.isEmpty()) {
            return
        }

        val restockableStacks = invStacks.filter { s -> s !in barrelStacks && s.item !is UpgradeItem }
        var itemsTaken = 0

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.loadFromStack(barrelStack)

            restockableStacks.forEach restockStack@{ restockStack ->
                if (!barrel.backingStorage.accepts(restockStack) || barrel.backingStorage.isEmpty) {
                    return@restockStack
                }

                val amountNeededForFullStack = restockStack.maxCount - restockStack.count
                val removableAmount = barrel.backingStorage.remove(
                    min(
                        amountNeededForFullStack.toLong(),
                        barrel.backingStorage.amountStored - 1
                    )
                ).toInt()

                restockStack.count += removableAmount
                itemsTaken += removableAmount
            }

            barrel.toStack(barrelStack)
        }

        if (itemsTaken > 0) {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel_hat.pulled_items", itemsTaken), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 1.1f)
        } else {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel_hat.no_pulled_items"), true)
            player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 0.5f, 1.0f)
        }

        player.inventory.markDirty()
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        val keyName = StockpileClient.BARREL_HAT_KEY.localizedName.toUpperCase()

        tooltip?.add(TranslatableText("ui.stockpile.barrel_hat").setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE))

        tooltip?.add(
            TranslatableText(
                "ui.stockpile.barrel_hat_push",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )

        tooltip?.add(
            TranslatableText(
                "ui.stockpile.barrel_hat_pull",
                keyName
            ).setStyle(UpgradeRegistry.UPGRADE_TOOLTIP_STYLE)
        )
    }
}