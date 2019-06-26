package me.branchpanic.mods.stockpile.content.client.gui

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeApplier
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeItem
import net.minecraft.ChatFormat
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun TextRenderer.drawWithBackground(
    text: String,
    x: Float,
    y: Float,
    foregroundColor: Int,
    backgroundColor: Int = 0x70000000,
    padding: Int = 1
) {
    val width = getStringWidth(text)
    Screen.fill(
        x.toInt() - padding,
        y.toInt() - padding,
        (x + width).toInt() + padding,
        (y + fontHeight).toInt() + padding,
        backgroundColor
    )
    draw(text, x, y, foregroundColor)
}

class UpgradeOverlayRenderer : OverlayRenderer {
    override fun draw(world: World, heldItem: ItemStack, selectedPos: BlockPos) {
        val mc = MinecraftClient.getInstance()
        val font = mc.textRenderer
        val textX = mc.window.scaledWidth / 2f + 16
        val textY = (mc.window.scaledHeight - font.fontHeight) / 2f

        val blockEntity = world.getBlockEntity(selectedPos)

        if (heldItem.item !is UpgradeItem || blockEntity !is UpgradeApplier) {
            return
        }

        val upgrade = (heldItem.item as UpgradeItem).getUpgrade(heldItem)
        val conflicts = upgrade.getConflictingUpgrades(blockEntity.appliedUpgrades)

        if (!blockEntity.canApplyUpgrade(upgrade)) {
            font.draw(
                I18n.translate("ui.stockpile.wrong_upgrade"),
                textX,
                textY,
                ChatFormat.RED.color ?: 0xFFFFFFFF.toInt()
            )
        } else if (conflicts.isNotEmpty()) {
            font.drawWithBackground(
                I18n.translate(
                    if (conflicts.size == 1) "ui.stockpile.upgrade_conflict" else "ui.stockpile.upgrade_conflicts",
                    conflicts.size
                ),
                textX,
                textY,
                0xFFFFFFFF.toInt(),
                0xE0FF2222.toInt()
            )

            font.drawWithBackground(
                I18n.translate(
                    "ui.stockpile.applied_upgrades",
                    blockEntity.appliedUpgrades.size,
                    blockEntity.maxUpgrades
                ),
                textX,
                textY + font.fontHeight + 2,
                0xFFFFFFFF.toInt()
            )

            blockEntity.appliedUpgrades.forEachIndexed { i, u ->
                font.drawWithBackground(
                    u.description.formattedText, textX, textY + (font.fontHeight + 2) * (i + 2),
                    if (conflicts.contains(u)) ChatFormat.RED.color ?: 0xFFFFFFFF.toInt() else 0xFF888888.toInt()
                )
            }
        } else {
            if (blockEntity.appliedUpgrades.size == blockEntity.maxUpgrades) {
                font.drawWithBackground(
                    I18n.translate("ui.stockpile.upgrades_maxed"),
                    textX,
                    textY,
                    ChatFormat.YELLOW.color ?: 0xFFFFFFFF.toInt()
                )
            } else {
                font.drawWithBackground(
                    I18n.translate("ui.stockpile.apply_upgrade"),
                    textX,
                    textY,
                    ChatFormat.GREEN.color ?: 0xFFFFFFFF.toInt()
                )
            }

            font.drawWithBackground(
                I18n.translate(
                    "ui.stockpile.applied_upgrades",
                    blockEntity.appliedUpgrades.size,
                    blockEntity.maxUpgrades
                ),
                textX,
                textY + font.fontHeight + 2,
                0xFFFFFFFF.toInt()
            )

            blockEntity.appliedUpgrades.forEachIndexed { i, u ->
                font.drawWithBackground(
                    u.description.formattedText, textX, textY + (font.fontHeight + 2) * (i + 2),
                    0xFF888888.toInt()
                )
            }
        }
    }
}