package me.branchpanic.mods.stockpile.content.client.gui

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeApplier
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import net.minecraft.ChatFormat
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class UpgradeOverlayRenderer : TextOverlayRenderer {
    override fun getLines(world: World, heldItem: ItemStack, selectedPos: BlockPos): List<OverlayTextComponent> {
        val blockEntity = world.getBlockEntity(selectedPos)

        if (heldItem.item !is UpgradeItem || blockEntity !is UpgradeApplier) {
            return emptyList()
        }

        val upgrade = (heldItem.item as UpgradeItem).getUpgrade(heldItem)
        val conflicts = upgrade.getConflictingUpgrades(blockEntity.appliedUpgrades)

        if (!blockEntity.canApplyUpgrade(upgrade)) {
            return listOf(OverlayTextComponent(I18n.translate("ui.stockpile.wrong_upgrade"), ChatFormat.RED.color!!))
        }

        val upgradeHeader = OverlayTextComponent(
            I18n.translate(
                "ui.stockpile.applied_upgrades", blockEntity.appliedUpgrades.size,
                blockEntity.maxUpgrades
            ),
            0xFFFFFFFF.toInt()
        )

        val upgradeLines = blockEntity.appliedUpgrades.mapIndexed { i, u ->
            OverlayTextComponent(
                "${i + 1}. ${u.description.formattedText}", if (u in conflicts) {
                    ChatFormat.RED.color!!
                } else {
                    ChatFormat.GRAY.color!!
                }
            )
        }

        if (conflicts.isNotEmpty()) {
            val status = OverlayTextComponent(
                I18n.translate(
                    if (conflicts.size == 1) "ui.stockpile.upgrade_conflict" else "ui.stockpile.upgrade_conflicts",
                    conflicts.size
                ),
                0xFFFFFFFF.toInt(),
                0xE0FF2222.toInt()
            )

            return listOf(status, upgradeHeader) + upgradeLines
        }

        if (blockEntity.appliedUpgrades.size == blockEntity.maxUpgrades) {
            val status = OverlayTextComponent(
                I18n.translate("ui.stockpile.upgrades_maxed"),
                ChatFormat.YELLOW.color!!
            )

            return listOf(status, upgradeHeader) + upgradeLines
        }

        val status = OverlayTextComponent(
            I18n.translate("ui.stockpile.apply_upgrade"),
            ChatFormat.GREEN.color!!
        )

        return listOf(status, upgradeHeader) + upgradeLines
    }
}