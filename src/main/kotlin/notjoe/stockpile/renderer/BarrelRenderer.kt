package notjoe.stockpile.renderer

import net.minecraft.block.BlockDirectional
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.util.EnumFacing
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.util.shorthand
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.min

const val BARREL_TRANSFORM_OFFSET = 1.0 / 512.0
const val BARREL_BAR_WIDTH = 18.0

class BarrelRenderer : TileEntityRenderer<TileBarrel>() {
    private val renderItem = Minecraft.getMinecraft().renderItem

    @Suppress("FunctionName")
    override fun render(
        tile: TileBarrel?,
        xPos: Double,
        yPos: Double,
        zPos: Double,
        partialTicks: Float,
        destroyStage: Int
    ) {
        if (tile == null) {
            return
        }

        val containedItem = tile.stackType
        if (containedItem.isEmpty) {
            return
        }

        val barrelFrontDirection = tile.blockState.getValue(BlockDirectional.FACING)

        GlStateManager.pushMatrix()
        renderDisplay(
            tile,
            barrelFrontDirection,
            xPos,
            yPos,
            zPos
        )
        GlStateManager.popMatrix()
    }

    // This method is adapted from CoFH Core's RenderUtils!
    private fun transformToFace(side: EnumFacing, xPos: Double, yPos: Double, zPos: Double) {
        when (side) {
            EnumFacing.NORTH -> {
                GlStateManager.translate(xPos + 0.75, yPos + 0.75, zPos + BARREL_TRANSFORM_OFFSET * 145)
            }
            EnumFacing.SOUTH -> {
                GlStateManager.translate(xPos + 0.25, yPos + 0.75, zPos + 1 - BARREL_TRANSFORM_OFFSET * 145)
                GlStateManager.rotate(180f, 0f, 1f, 0f)
            }
            EnumFacing.WEST -> {
                GlStateManager.translate(xPos + BARREL_TRANSFORM_OFFSET * 145, yPos + 0.75, zPos + 0.25)
                GlStateManager.rotate(90f, 0f, 1f, 0f)
            }
            EnumFacing.EAST -> {
                GlStateManager.translate(xPos + 1 - BARREL_TRANSFORM_OFFSET * 145, yPos + 0.75, zPos + 0.75)
                GlStateManager.rotate(-90f, 0f, 1f, 0f)
            }
            EnumFacing.UP -> {
                GlStateManager.translate(xPos + 0.75, yPos + 1 - BARREL_TRANSFORM_OFFSET * 145, zPos + 0.75)
                GlStateManager.rotate(90f, 1f, 0f, 0f)
            }
            EnumFacing.DOWN -> {
                GlStateManager.translate(xPos + 0.75, yPos + BARREL_TRANSFORM_OFFSET * 145, zPos + 0.25)
                GlStateManager.rotate(-90f, 1f, 0f, 0f)
            }
        }
    }

    /**
     * Draws a rectangle using the Tessellator at z = 0.
     * @param x1 X-coordinate of the bottom-left corner of the rectangle.
     * @param y1 Y-coordinate of the bottom-left corner of the rectangle.
     * @param x2 X-coordinate of the top-left corner.
     * @param y2 Y-coordinate of the top-left corner.
     */
    private fun drawFlatRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: Color) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(x2, y1, 0.0)
            .color(color)
            .endVertex()
        buffer.pos(x2, y2, 0.0)
            .color(color)
            .endVertex()
        buffer.pos(x1, y2, 0.0)
            .color(color)
            .endVertex()
        buffer.pos(x1, y1, 0.0)
            .color(color)
            .endVertex()

        tessellator.draw()
    }

    private fun renderProgressBar(text: String, filledAmount: Double, xCenter: Float, yCenter: Float, textColor: Int) {
        GlStateManager.translate(0.0, 0.0, 0.315 * 1 / BARREL_TRANSFORM_OFFSET)
        GlStateManager.scale(0.5, 0.5, 1.0)

        val textWidth = fontRenderer.getStringWidth(text)
        val textHeight = fontRenderer.FONT_HEIGHT
        val textCenterX = xCenter * 2 - (textWidth / 2)
        val textCenterY = yCenter * 2 - (textHeight / 2)

        GlStateManager.disableTexture2D()

        val filledBarWidth = 2 * min(filledAmount, 1.0) * BARREL_BAR_WIDTH
        val unfilledBarWidth = 2 * BARREL_BAR_WIDTH - filledBarWidth

        if (filledBarWidth > 0) {
            drawFlatRectangle(
                -0.25 * BARREL_BAR_WIDTH,
                textCenterY + textHeight.toDouble(),
                filledBarWidth - 0.25 * BARREL_BAR_WIDTH,
                textCenterY - 0.25 * textHeight,
                Color(0f, 0f, 1f, 0.7f)
            )
        }

        if (unfilledBarWidth > 0) {
            drawFlatRectangle(
                filledBarWidth - 0.25 * BARREL_BAR_WIDTH,
                textCenterY + textHeight.toDouble(),
                filledBarWidth + unfilledBarWidth,
                textCenterY - 0.25 * textHeight,
                Color(0f, 0f, 0f, 0.7f)
            )
        }

        GlStateManager.enableTexture2D()

        GlStateManager.translate(0.0, 0.0, 0.02)
        fontRenderer.drawString(text, textCenterX, textCenterY, textColor)
    }

    private fun renderDisplay(
        tile: TileBarrel,
        side: EnumFacing,
        xPos: Double,
        yPos: Double,
        zPos: Double
    ) {
        val stack = tile.stackType
        val amount = tile.amountStored
        val maxItems = tile.maxStacks * tile.inventoryStackLimit
        val tilePos = tile.pos
        val isLocked = tile.typeIsLocked

        if (stack.isEmpty) {
            return
        }

        GlStateManager.pushMatrix()

        transformToFace(side, xPos, yPos, zPos)

        GlStateManager.scale(0.03125, 0.03125, -BARREL_TRANSFORM_OFFSET)
        GlStateManager.rotate(180f, 0f, 0f, 1f)

        val renderSide = tilePos.offset(side)
        if (!world.getBlockState(renderSide).isFullCube) {
            val lightmapCombined = world.getCombinedLight(renderSide, 3)
            val lightmapU = lightmapCombined % 65536
            val lightmapV = lightmapCombined / 65536
            OpenGlHelper.setLightmapTextureCoords(
                OpenGlHelper.lightmapTexUnit,
                lightmapU.toFloat(),
                lightmapV.toFloat()
            )
        }

        renderItem.renderItemAndEffectIntoGUI(stack, 0, -3)

        val displayText = if (amount > 0) amount.shorthand() else I18n.format("stockpile.barrel.empty")

        renderProgressBar(
            displayText + if (!isLocked) "*" else "",
            amount.toDouble() / maxItems, 8f, 16f, if (maxItems - amount <= 0) 0xFFFF22 else 0xFFFFFF
        )

        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.popMatrix()

        RenderHelper.enableStandardItemLighting()
    }
}

private fun BufferBuilder.color(javaColor: Color): BufferBuilder {
    return color(javaColor.red / 255f, javaColor.green / 255f, javaColor.blue / 255f, javaColor.alpha / 255f)
}
