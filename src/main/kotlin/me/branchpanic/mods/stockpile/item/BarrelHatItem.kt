package me.branchpanic.mods.stockpile.item

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.Stockpile.id
import me.branchpanic.mods.stockpile.client.StockpileClient
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.util.giveTo
import me.branchpanic.mods.stockpile.util.withCount
import me.branchpanic.mods.stockpile.impl.storage.toQuantifier
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
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
        return player.inventory.main + player.inventory.offHand + player.inventory.armor
    }

    private fun getBarrelStacks(player: PlayerEntity, warnOnStacked: Boolean = true): List<ItemStack> {
        val stacks = getInventoryStacks(player)

        // TODO: Replace with barrel hat interface
        val potentialStacks = stacks.filter { s -> s.item == Registry.ITEM[id("item_barrel")] }

        if (warnOnStacked && potentialStacks.any { s -> s.count > 1 }) {
            player.sendMessage(
                TranslatableText("ui.stockpile.barrel_hat.stacked_warning").setStyle(Style.EMPTY.withColor(Formatting.GRAY)),
                false
            )
        }

        return potentialStacks.filter { s -> s.count == 1 }
    }

    private fun getTransferableStacks(player: PlayerEntity): List<ItemStack> {
        val barrelStacks = getBarrelStacks(player, warnOnStacked = false)
        return getInventoryStacks(player).filterNot { s -> s in barrelStacks }
    }

    fun pushInventoryToBarrels(player: PlayerEntity) {
        val barrelStacks = getBarrelStacks(player)

        if (barrelStacks.isEmpty()) {
            return
        }

        val insertableStacks = getTransferableStacks(player)
        var itemsDeposited = 0

        barrelStacks.forEach { barrelStack ->
            // Can't map because it may change in this loop
            val barrel = ItemBarrelBlockEntity.fromStack(barrelStack)

            insertableStacks.forEach insertStack@{ insertStack ->
                if (!barrel.storage.contents.canMergeWith(insertStack.toQuantifier()) || insertStack.isEmpty) {
                    return@insertStack
                }

                val amount = insertStack.count

                if (amount <= 1) {
                    return@insertStack
                }

                val maxInsertableStack = insertStack.withCount(amount - 1)
                val remainder = barrel.storage.addAtMost(maxInsertableStack.toQuantifier()).amount + 1

                insertStack.count = remainder.toInt()
                itemsDeposited += amount - remainder.toInt()
            }

            barrelStack.setSubNbt(ItemBarrelBlockEntity.STORED_BLOCK_ENTITY_TAG, barrel.toClientTag(NbtCompound()))
        }

        if (itemsDeposited > 0) {
            player.sendMessage(TranslatableText("ui.stockpile.barrel_hat.pushed_items", itemsDeposited), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 0.9f)
        } else {
            player.sendMessage(TranslatableText("ui.stockpile.barrel_hat.no_pushed_items"), true)
            player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 0.5f, 1.0f)
        }
        player.inventory.markDirty()
    }

    fun pullInventoryFromBarrels(player: PlayerEntity) {
        val invStacks = getInventoryStacks(player)
        val barrelStacks = getBarrelStacks(player)

        if (barrelStacks.isEmpty()) {
            return
        }

        val restockableStacks = invStacks.filter { s -> s !in barrelStacks }
        var itemsTaken = 0

        barrelStacks.forEach { barrelStack ->
            val barrel = ItemBarrelBlockEntity.fromStack(barrelStack)

            restockableStacks.forEach restockStack@{ restockStack ->
                if (!barrel.storage.contents.canMergeWith(restockStack.toQuantifier()) || barrel.storage.isEmpty) {
                    return@restockStack
                }

                val amountNeededForFullStack = restockStack.maxCount - restockStack.count
                val dispensedStack = barrel.storage.contents.reference.copy()
                val removableAmount = barrel.storage.removeAtMost(
                    min(
                        amountNeededForFullStack.toLong(),
                        barrel.storage.contents.amount - 1
                    )
                ).toInt()

                if (!restockStack.isEmpty) {
                    restockStack.count += removableAmount
                } else {
                    dispensedStack.withCount(removableAmount).giveTo(player)
                }

                itemsTaken += removableAmount
            }

            barrelStack.setSubNbt(ItemBarrelBlockEntity.STORED_BLOCK_ENTITY_TAG, barrel.toClientTag(NbtCompound()))
        }

        if (itemsTaken > 0) {
            player.sendMessage(TranslatableText("ui.stockpile.barrel_hat.pulled_items", itemsTaken), true)
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 1.1f)
        } else {
            player.sendMessage(TranslatableText("ui.stockpile.barrel_hat.no_pulled_items"), true)
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
        val keyName = StockpileClient.barrelHatBinding.boundKeyLocalizedText.string.uppercase()

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
