package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.*
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.MatrixStack
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

fun drawRectangle(buffer: VertexConsumer, x1: Double, y1: Double, x2: Double, y2: Double, color: ArgbColor) {
    buffer.vertex(x2, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).texture(0f, 0f).light(0).normal(0f, 0f, 0f).next()
    buffer.vertex(x2, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).texture(0f, 0f).light(0).normal(0f, 0f, 0f).next()
    buffer.vertex(x1, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).texture(0f, 0f).light(0).normal(0f, 0f, 0f).next()
    buffer.vertex(x1, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).texture(0f, 0f).light(0).normal(0f, 0f, 0f).next()
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
    matrixStack: MatrixStack,
    vertexConsumerProvider: VertexConsumerProvider,
    settings: FillBarSettings,
    textRenderer: TextRenderer,
    value: Long,
    maxValue: Long,
    clearWhenEmpty: Boolean,
    xCenter: Double,
    yCenter: Double,
    i: Int,
    j: Int
) {
    matrixStack.translate(0.0, 0.0, 0.3 * 1 / COFH_TRANSFORM_OFFSET)
    matrixStack.scale(0.5f, 0.5f, 1.0f)

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

    val buffer = vertexConsumerProvider.getBuffer(RenderLayer.getEndPortal(1))
    if (filledBarWidth > 0) {
        drawRectangle(
            buffer,
            -0.25 * settings.width,
            textCenterY + textHeight,
            filledBarWidth - 0.25 * settings.width,
            textCenterY - 0.25 * textHeight,
            settings.foregroundColor
        )
    }

    if (unfilledBarWidth > 0) {
        drawRectangle(
            buffer,
            filledBarWidth - 0.25 * settings.width,
            textCenterY + textHeight,
            filledBarWidth + unfilledBarWidth - 0.25 * settings.width,
            textCenterY - 0.25 * textHeight,
            settings.backgroundColor
        )
    }

    matrixStack.translate(0.0, 0.0, 0.02)
    textRenderer.draw(
        displayText, textCenterX.toFloat(), textCenterY.toFloat(),
        textColor, false, matrixStack.peek().model, vertexConsumerProvider, false, 0, i
    )
}
