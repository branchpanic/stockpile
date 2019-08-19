package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.resource.language.I18n
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

data class FillBarSettings(
    val backgroundColor: ArgbColor,
    val foregroundColor: ArgbColor,
    val textColor: ArgbColor,
    val textColorFull: ArgbColor,
    val width: Double
)

fun drawRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: ArgbColor) {
    val tessellator = Tessellator.getInstance()
    val bufferBuilder = tessellator.bufferBuilder

    bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)
    bufferBuilder.vertex(x2, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
    bufferBuilder.vertex(x2, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
    bufferBuilder.vertex(x1, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
    bufferBuilder.vertex(x1, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).next()

    tessellator.draw()
}

// TODO(i18n): Localize quantities
val NUMBER_SUFFIXES = arrayOf("k", "M", "B", "T", "Q")

fun Long.abbreviate(): String {
    val orderOfMagnitude = log10(abs(toDouble())).toInt()
    if (orderOfMagnitude < 4) {
        return "%,d".format(this)
    }

    val displayMagnitude = orderOfMagnitude / 3
    val suffix = NUMBER_SUFFIXES[min(displayMagnitude - 1, NUMBER_SUFFIXES.size)]
    val displayNumber = "%.1f".format(this / 10.0.pow(3 * displayMagnitude))

    return displayNumber + suffix
}

fun renderFillBar(
    settings: FillBarSettings,
    textRenderer: TextRenderer,
    value: Long,
    maxValue: Long,
    clearWhenEmpty: Boolean,
    xCenter: Double,
    yCenter: Double
) {
    GlStateManager.translated(0.0, 0.0, 0.3 * 1 / COFH_TRANSFORM_OFFSET)
    GlStateManager.scaled(0.5, 0.5, 1.0)

    val filledAmount = value.toDouble() / maxValue

    val displayText = if (filledAmount <= 0) {
        I18n.translate("ui.stockpile.empty")
    } else {
        value.abbreviate() + if (clearWhenEmpty) "*" else ""
    }

    val textColor = if (filledAmount >= 1) {
        settings.textColorFull
    } else {
        settings.textColor
    }

    val textWidth = textRenderer.getStringWidth(displayText)
    val textHeight = textRenderer.fontHeight
    val textCenterX = xCenter * 2 - (textWidth / 2)
    val textCenterY = yCenter * 2 - (textHeight / 2)

    val totalBarWidth = 2 * settings.width + 0.25 * settings.width
    val filledBarWidth = totalBarWidth * min(filledAmount, 1.0)
    val unfilledBarWidth = totalBarWidth - filledBarWidth

    GlStateManager.disableTexture()

    if (filledBarWidth > 0) {
        drawRectangle(
            -0.25 * settings.width,
            textCenterY + textHeight,
            filledBarWidth - 0.25 * settings.width,
            textCenterY - 0.25 * textHeight,
            settings.foregroundColor
        )
    }

    if (unfilledBarWidth > 0) {
        drawRectangle(
            filledBarWidth - 0.25 * settings.width,
            textCenterY + textHeight,
            filledBarWidth + unfilledBarWidth - 0.25 * settings.width,
            textCenterY - 0.25 * textHeight,
            settings.backgroundColor
        )
    }

    GlStateManager.enableTexture()

    GlStateManager.translated(0.0, 0.0, 0.02)
    textRenderer.draw(displayText, textCenterX.toFloat(), textCenterY.toFloat(), textColor)
}
