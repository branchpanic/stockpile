package me.branchpanic.mods.stockpile.content.client.gui

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun TextRenderer.drawWithBackground(
    text: String,
    x: Float,
    y: Float,
    boxWidth: Float,
    foregroundColor: Int,
    backgroundColor: Int = 0x70000000,
    padding: Float = 1f
) {
    fill4f(
        x,
        y,
        (x + boxWidth) + 2 * padding,
        (y + fontHeight) + 2 * padding,
        backgroundColor
    )

    draw(text, (x + padding), (y + padding), foregroundColor)
}

fun fill4f(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
    val a = (color shr 24 and 255).toFloat() / 255.0f
    val r = (color shr 16 and 255).toFloat() / 255.0f
    val g = (color shr 8 and 255).toFloat() / 255.0f
    val b = (color and 255).toFloat() / 255.0f

    val tess = Tessellator.getInstance()
    val buf = tess.buffer

    GlStateManager.enableBlend()
    GlStateManager.disableTexture()
    GlStateManager.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA.value,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value,
        GlStateManager.SourceFactor.ONE.value,
        GlStateManager.DestFactor.ZERO.value
    )

    GlStateManager.color4f(r, g, b, a)
    buf.begin(7, VertexFormats.POSITION)
    buf.vertex(x1.toDouble(), y2.toDouble(), 0.0).next()
    buf.vertex(x2.toDouble(), y2.toDouble(), 0.0).next()
    buf.vertex(x2.toDouble(), y1.toDouble(), 0.0).next()
    buf.vertex(x1.toDouble(), y1.toDouble(), 0.0).next()
    tess.draw()
    GlStateManager.enableTexture()
    GlStateManager.disableBlend()
}

data class OverlayTextComponent(val text: String, val foregroundColor: Int, val backgroundColor: Int = 0x70000000)

interface OverlayRenderer {
    fun draw(world: World, heldItem: ItemStack, selectedPos: BlockPos)
}

interface TextOverlayRenderer : OverlayRenderer {

    fun getStatusColor(world: World, heldItem: ItemStack, selectedPos: BlockPos): Int? = null

    fun getLines(world: World, heldItem: ItemStack, selectedPos: BlockPos): List<OverlayTextComponent>

    override fun draw(world: World, heldItem: ItemStack, selectedPos: BlockPos) {
        val mc = MinecraftClient.getInstance()
        val font = mc.textRenderer

        val padding = 1f
        val textX = (mc.window.scaledWidth / 2f + 16) - padding

        val lines = getLines(world, heldItem, selectedPos)

        if (lines.isEmpty()) {
            return
        }

        val textHeight = lines.size * (2 * padding + font.fontHeight)
        val textY = ((mc.window.scaledHeight - textHeight) / 2f) - padding

        val longestLine = lines.map { c -> font.getStringWidth(c.text) }.max() ?: throw IllegalStateException()

        lines.forEachIndexed { i, c ->
            font.drawWithBackground(
                c.text,
                textX,
                (textY + i * (2 * padding + font.fontHeight)),
                longestLine.toFloat(),
                c.foregroundColor,
                c.backgroundColor,
                padding
            )
        }
    }
}
