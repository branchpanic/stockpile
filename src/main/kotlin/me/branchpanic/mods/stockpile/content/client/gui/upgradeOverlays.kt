package me.branchpanic.mods.stockpile.content.client.gui

import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import me.branchpanic.mods.stockpile.content.item.UpgradeRemoverItem
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun getUpgradeList(
    container: UpgradeContainer,
    conflicts: List<Upgrade> = emptyList(),
    removals: List<Upgrade> = emptyList()
): List<OverlayTextComponent> {
    return listOf(
        OverlayTextComponent(
            I18n.translate(
                "ui.stockpile.applied_upgrades", container.appliedUpgrades.size,
                container.maxUpgrades
            ),
            0xFFFFFFFF.toInt()
        )
    ) + container.appliedUpgrades.mapIndexed { i, u ->
        val prefix = if (u in removals) Formatting.STRIKETHROUGH.toString() else ""

        OverlayTextComponent(
            "${i + 1}. ${prefix + u.description.asFormattedString()}", if (u in conflicts) {
                Formatting.RED.colorValue!!
            } else {
                Formatting.GRAY.colorValue!!
            }
        )
    }
}

class UpgradeOverlayRenderer : TextOverlayRenderer {
    override fun getLines(world: World, heldItem: ItemStack, selectedPos: BlockPos): List<OverlayTextComponent> {
        val blockEntity = world.getBlockEntity(selectedPos)

        if (heldItem.item !is UpgradeItem || blockEntity !is UpgradeContainer) {
            return emptyList()
        }

        val upgrade = (heldItem.item as UpgradeItem).getUpgrade(heldItem)
        val conflicts = upgrade.getConflictingUpgrades(blockEntity.appliedUpgrades)

        if (!blockEntity.isUpgradeTypeAllowed(upgrade)) {
            return listOf(
                OverlayTextComponent(
                    I18n.translate("ui.stockpile.wrong_upgrade"),
                    0xFFFFFFFF.toInt(),
                    0xE0FF2222.toInt()
                )
            )
        }

        val upgradeLines = getUpgradeList(blockEntity, conflicts = conflicts)

        if (conflicts.isNotEmpty()) {
            val status = OverlayTextComponent(
                I18n.translate(
                    if (conflicts.size == 1) "ui.stockpile.upgrade_conflict" else "ui.stockpile.upgrade_conflicts",
                    conflicts.size
                ),
                0xFFFFFFFF.toInt(),
                0xE0FF2222.toInt()
            )

            return listOf(status) + upgradeLines
        }

        if (blockEntity.appliedUpgrades.size == blockEntity.maxUpgrades) {
            val status = OverlayTextComponent(
                I18n.translate("ui.stockpile.upgrades_maxed"),
                Formatting.YELLOW.colorValue!!
            )

            return listOf(status) + upgradeLines
        }

        val status = OverlayTextComponent(
            I18n.translate("ui.stockpile.apply_upgrade"),
            Formatting.GREEN.colorValue!!
        )

        return listOf(status) + upgradeLines
    }
}

class UpgradeRemoverOverlayRenderer : TextOverlayRenderer {
    override fun getLines(world: World, heldItem: ItemStack, selectedPos: BlockPos): List<OverlayTextComponent> {
        val blockEntity = world.getBlockEntity(selectedPos)

        if (heldItem.item !is UpgradeRemoverItem || blockEntity !is UpgradeContainer) {
            return emptyList()
        }

        if (blockEntity.appliedUpgrades.isEmpty()) {
            return listOf(
                OverlayTextComponent(
                    I18n.translate("ui.stockpile.no_upgrades_to_remove"),
                    0xFFFFFFFF.toInt(),
                    0xE0FF2222.toInt()
                )
            ) + getUpgradeList(blockEntity)
        }

        val upgradeToRemove = blockEntity.appliedUpgrades.last()

        if (!upgradeToRemove.canSafelyBeRemovedFrom(blockEntity)) {
            return listOf(
                OverlayTextComponent(
                    I18n.translate("ui.stockpile.cant_remove_upgrade"),
                    0xFFFFFFFF.toInt(),
                    0xE0FF2222.toInt()
                )
            ) + getUpgradeList(blockEntity, conflicts = listOf(upgradeToRemove))
        }

        return listOf(
            OverlayTextComponent(
                I18n.translate("ui.stockpile.use_to_remove"),
                Formatting.GREEN.colorValue!!
            )
        ) + getUpgradeList(blockEntity, removals = listOf(upgradeToRemove))
    }
}