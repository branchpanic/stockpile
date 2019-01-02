package notjoe.stockpile.renderer

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.{DstBlendFactor, SrcBlendFactor}
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.{GuiLighting, Tessellator, VertexFormats}
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity
import org.lwjgl.opengl.GL11

import scala.swing.Color

// This is a bit of a trainwreck right now, so please excuse all of the magic numbers and weird operations...
// Before cleaning it up, I'd at least like to get it working.

@Environment(EnvType.CLIENT)
object StockpileBarrelRenderer extends BlockEntityRenderer[StockpileBarrelBlockEntity] {
  final val TESSELLATOR = Tessellator.getInstance()

  final val BAR_WIDTH = 18.0

  // From CoFH Core's RenderUtils
  final val TRANSFORM_OFFSET = 1.0 / 512.0

  override def render(barrel: StockpileBarrelBlockEntity,
                      x: Double,
                      y: Double,
                      z: Double,
                      partialTicks: Float,
                      progress: Int): Unit = {
    if (barrel.inventory.isAcceptingNewStackType) {
      return
    }

    val frontFaceDirection = barrel.getCachedState.get(Properties.FACING)

    GlStateManager.pushMatrix()
    renderDisplay(barrel, frontFaceDirection, x, y, z)
    GlStateManager.popMatrix()
  }

  def transformToFace(barrelFrontDirection: Direction, x: Double, y: Double, z: Double): Unit = {
    // Placeholder-- we're just always assuming north for now. 16.6% of the time it works every time.
    GlStateManager.translated(x + 0.75, y + 0.75, z + TRANSFORM_OFFSET * 145)
  }

  def renderFillBar(displayText: String, filledAmount: Double, xCenter: Float, yCenter: Float, textColor: Int): Unit = {
    GlStateManager.translated(0.0, 0.0, 0.315 * 1 / TRANSFORM_OFFSET)
    GlStateManager.scaled(0.5, 0.5, 1.0)

    // TODO: Remove sketchy math from the original. Seriously, why does -0.25 show up so much?!

    val textWidth = getFontRenderer.getStringWidth(displayText)
    val textHeight = getFontRenderer.fontHeight
    val textCenterX = xCenter * 2 - (textWidth / 2)
    val textCenterY = yCenter * 2 - (textHeight / 2)
    val totalBarWidth = 2 * BAR_WIDTH + 0.25 * BAR_WIDTH
    val filledBarWidth = totalBarWidth * Math.min(filledAmount, 1.0)
    val unfilledBarWidth = totalBarWidth - filledBarWidth

    GlStateManager.disableTexture()

    if (filledBarWidth > 0) {
      drawRectangle(
        -0.25 * BAR_WIDTH,
        textCenterY + textHeight,
        filledBarWidth - 0.25 * BAR_WIDTH,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 1f, 0.7f)
      )
    }

    if (unfilledBarWidth > 0) {
      drawRectangle(
        filledBarWidth - 0.25 * BAR_WIDTH,
        textCenterY + textHeight,
        filledBarWidth + unfilledBarWidth - 0.25 * BAR_WIDTH,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 0f, 0.7f)
      )
    }

    GlStateManager.enableTexture()

    GlStateManager.translated(0.0, 0.0, 0.02)
    getFontRenderer.draw(displayText, textCenterX, textCenterY, textColor)
  }

  def renderDisplay(barrel: StockpileBarrelBlockEntity,
                    frontFaceDirection: Direction,
                    x: Double,
                    y: Double,
                    z: Double): Unit = {
    val stack = barrel.inventory.stackType
    val amount = barrel.inventory.amountStored
    val capacity = barrel.inventory.maxStacks * barrel.inventory.stackSize
    val position = barrel.getPos
    val isLocked = !barrel.inventory.allowNewStackWhenEmpty


    GlStateManager.enableRescaleNormal()
    GlStateManager.alphaFunc(516, 0.1F)
    GlStateManager.enableBlend()
    GuiLighting.enable()
    GlStateManager.blendFuncSeparate(SrcBlendFactor.SRC_ALPHA, DstBlendFactor.ONE_MINUS_SRC_ALPHA, SrcBlendFactor.ONE, DstBlendFactor.ZERO)

    transformToFace(frontFaceDirection, x, y, z)

    GlStateManager.scaled(0.03125, 0.03125, -TRANSFORM_OFFSET)
    GlStateManager.rotated(180, 0, 0, 1)

    MinecraftClient.getInstance().getItemRenderer.renderItemAndGlowInGui(stack, 0, -3)

    renderFillBar("TODO", amount / capacity, 8f, 16f, if (capacity - amount <= 0) {
      0xFFFF22
    } else {
      0xFFFFFF
    })

    GlStateManager.disableRescaleNormal()
    GlStateManager.disableBlend()
  }

  def drawRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: Color): Unit = {
    val bufferBuilder = TESSELLATOR.getBufferBuilder

    bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)

    Seq(
      (x2, y1),
      (x2, y2),
      (x1, y2),
      (x1, y1)
    ).foreach { case (i, j) =>
      bufferBuilder.vertex(i, j, 0)
        .color(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
        .next()
    }

    TESSELLATOR.draw()
  }
}
