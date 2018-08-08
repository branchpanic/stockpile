package notjoe.stockpile.renderer

import net.minecraft.block.BlockDirectional
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.math.BlockPos
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.util.shorthand
import org.lwjgl.opengl.GL11

const val BARREL_TRANSFORM_OFFSET = 1.0 / 512.0

class BarrelRenderer : TileEntityRenderer<TileBarrel>() {
    private val renderItem = Minecraft.getMinecraft().renderItem

    @Suppress("FunctionName")
    override fun func_199341_a(tile: TileBarrel?, xPos: Double, yPos: Double, zPos: Double, partialTicks: Float,
                               destroyStage: Int) {
        if (tile == null) {
            return
        }

        val containedItem = tile.stackType
        if (containedItem.isEmpty) {
            return
        }

        val barrelFrontDirection = tile.blockState.getValue(BlockDirectional.FACING)

        GlStateManager.pushMatrix()
        renderDisplay(containedItem, tile.amountStored, tile.availableSpace, tile.pos, barrelFrontDirection, xPos, yPos, zPos)
        GlStateManager.popMatrix()
    }

    // This method is adapted from CoFH Core's RenderUtils!
    private fun transformToFace(side: EnumFacing, xPos: Double, yPos: Double, zPos: Double) {
        when (side) {
            NORTH -> {
                GlStateManager.translate(xPos + 0.75, yPos + 0.75, zPos + BARREL_TRANSFORM_OFFSET * 145)
            }
            SOUTH -> {
                GlStateManager.translate(xPos + 0.25, yPos + 0.75, zPos + 1 - BARREL_TRANSFORM_OFFSET * 145)
                GlStateManager.rotate(180f, 0f, 1f, 0f)
            }
            WEST -> {
                GlStateManager.translate(xPos + BARREL_TRANSFORM_OFFSET * 145, yPos + 0.75, zPos + 0.25)
                GlStateManager.rotate(90f, 0f, 1f, 0f)
            }
            EAST -> {
                GlStateManager.translate(xPos + 1 - BARREL_TRANSFORM_OFFSET * 145, yPos + 0.75, zPos + 0.75)
                GlStateManager.rotate(-90f, 0f, 1f, 0f)
            }
            UP -> {
                GlStateManager.translate(xPos + 0.75, yPos + 1 - BARREL_TRANSFORM_OFFSET * 145, zPos + 0.75)
                GlStateManager.rotate(90f, 1f, 0f, 0f)
            }
            DOWN -> {
                GlStateManager.translate(xPos + 0.75, yPos + BARREL_TRANSFORM_OFFSET * 145, zPos + 0.25)
                GlStateManager.rotate(-90f, 1f, 0f, 0f)
            }
        }
    }

    private fun renderDisplayText(text: String, xCenter: Float, yCenter: Float, color: Int) {
        GlStateManager.translate(0.0, 0.0, 0.315 * 1 / BARREL_TRANSFORM_OFFSET)
        GlStateManager.scale(0.5, 0.5, 1.0)

        val textWidth = fontRenderer.getStringWidth(text)
        val textHeight = fontRenderer.FONT_HEIGHT
        val textCenterX = xCenter * 2 - (textWidth / 2)
        val textCenterY = yCenter * 2 - (textHeight / 2)

        GlStateManager.disableTexture2D()

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        buffer.pos((textCenterX + textWidth * 1.25), (textCenterY + textHeight).toDouble(), 0.0)
                .color(0.0f, 0.0f, 0.0f, 0.4f)
                .endVertex()
        buffer.pos((textCenterX + textWidth * 1.25), (textCenterY - textHeight / 4).toDouble(), 0.0)
                .color(0.0f, 0.0f, 0.0f, 0.4f)
                .endVertex()
        buffer.pos((textCenterX - textWidth / 3.75), (textCenterY - textHeight / 4).toDouble(), 0.0)
                .color(0.0f, 0.0f, 0.0f, 0.4f)
                .endVertex()
        buffer.pos((textCenterX - textWidth / 3.75), (textCenterY + textHeight).toDouble(), 0.0)
                .color(0.0f, 0.0f, 0.0f, 0.4f)
                .endVertex()
        tessellator.draw()

        GlStateManager.enableTexture2D()

        GlStateManager.translate(0.0, 0.0, 0.02)
        fontRenderer.func_211126_b(text, textCenterX, textCenterY, color)
    }

    private fun renderDisplay(stack: ItemStack, amount: Int, availableSpace: Int, tilePos: BlockPos,
                              side: EnumFacing, xPos: Double, yPos: Double, zPos: Double) {
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
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapU.toFloat(), lightmapV.toFloat())
        }

        renderItem.renderItemAndEffectIntoGUI(stack, 0, -3)

        renderDisplayText(if (amount > 0) amount.shorthand() else I18n.format("stockpile.barrel.empty"),
                8f, 16f, if (availableSpace <= 0) 0xFFFF22 else 0xFFFFFF)

        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.popMatrix()

        RenderHelper.enableStandardItemLighting()
    }
}