package notjoe.stockpile.renderer

import java.awt.Color

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.{DestFactor, SourceFactor}
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.{GuiLighting, Tessellator, VertexFormats}
import net.minecraft.client.resource.language.I18n
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity
import notjoe.stockpile.extension.IntExtensions._
import notjoe.stockpile.inventory.MassItemInventory
import org.lwjgl.opengl.GL11

@Environment(EnvType.CLIENT)
object StockpileBarrelRenderer extends BlockEntityRenderer[StockpileBarrelBlockEntity] {
  val FillBarWidth = 18.0
  val CofhTransformOffset: Double = 1.0 / 512.0

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
    renderFrontDisplay(barrel, frontFaceDirection, x, y, z)
    GlStateManager.popMatrix()
  }

  def transformToFace(orientation: Direction, x: Double, y: Double, z: Double): Unit = orientation match {
    case Direction.NORTH => {
      GlStateManager.translated(x + 0.75, y + 0.75, z + CofhTransformOffset * 145)
    }
    case Direction.SOUTH => {
      GlStateManager.translated(x + 0.25, y + 0.75, z + 1 - CofhTransformOffset * 145)
      GlStateManager.rotated(180f, 0f, 1f, 0f)
    }
    case Direction.WEST => {
      GlStateManager.translated(x + CofhTransformOffset * 145, y + 0.75, z + 0.25)
      GlStateManager.rotated(90f, 0f, 1f, 0f)
    }
    case Direction.EAST => {
      GlStateManager.translated(x + 1 - CofhTransformOffset * 145, y + 0.75, z + 0.75)
      GlStateManager.rotated(-90f, 0f, 1f, 0f)
    }
    case Direction.UP => {
      GlStateManager.translated(x + 0.75, y + 1 - CofhTransformOffset * 145, z + 0.75)
      GlStateManager.rotated(90f, 1f, 0f, 0f)
    }
    case Direction.DOWN => {
      GlStateManager.translated(x + 0.75, y + CofhTransformOffset * 145, z + 0.25)
      GlStateManager.rotated(-90f, 1f, 0f, 0f)
    }
  }

  def renderFillBar(inventory: MassItemInventory, xCenter: Float, yCenter: Float): Unit = {
    GlStateManager.translated(0.0, 0.0, 0.3 * 1 / CofhTransformOffset)
    GlStateManager.scaled(0.5, 0.5, 1.0)

    val displayText = getDisplayString(inventory)
    val amount = inventory.amountStored
    val capacity = inventory.maxStacks * inventory.stackSize
    val filledAmount = amount.toDouble / capacity
    val textColor = if (capacity > amount) {
      0xFFFFFF
    } else {
      0xFFFF22
    }

    val textWidth = getFontRenderer.getStringWidth(displayText)
    val textHeight = getFontRenderer.fontHeight
    val textCenterX = xCenter * 2 - (textWidth / 2)
    val textCenterY = yCenter * 2 - (textHeight / 2)

    val totalBarWidth = 2 * FillBarWidth + 0.25 * FillBarWidth
    val filledBarWidth = totalBarWidth * Math.min(filledAmount, 1.0)
    val unfilledBarWidth = totalBarWidth - filledBarWidth

    GlStateManager.disableTexture()

    if (filledBarWidth > 0) {
      drawRectangle(
        -0.25 * FillBarWidth,
        textCenterY + textHeight,
        filledBarWidth - 0.25 * FillBarWidth,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 1f, 0.7f)
      )
    }

    if (unfilledBarWidth > 0) {
      drawRectangle(
        filledBarWidth - 0.25 * FillBarWidth,
        textCenterY + textHeight,
        filledBarWidth + unfilledBarWidth - 0.25 * FillBarWidth,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 0f, 0.7f)
      )
    }

    GlStateManager.enableTexture()

    GlStateManager.translated(0.0, 0.0, 0.02)
    getFontRenderer.draw(displayText, textCenterX, textCenterY, textColor)
  }

  def renderFrontDisplay(barrel: StockpileBarrelBlockEntity,
                         orientation: Direction,
                         x: Double,
                         y: Double,
                         z: Double): Unit = {
    val stack = barrel.inventory.stackType

    GlStateManager.enableRescaleNormal()
    GlStateManager.alphaFunc(516, 0.1F)
    GlStateManager.enableBlend()
    GuiLighting.enable()
    GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO)
    GlStateManager.pushMatrix()

    transformToFace(orientation, x, y, z)

    GlStateManager.scaled(0.03125, 0.03125, -CofhTransformOffset)
    GlStateManager.rotated(180, 0, 0, 1)

    MinecraftClient.getInstance().getItemRenderer.renderItemAndGlowInGui(stack, 0, -3)
    renderFillBar(barrel.inventory, 8f, 16f)

    GlStateManager.popMatrix()
    GlStateManager.disableRescaleNormal()
    GlStateManager.disableBlend()
  }

  def drawRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: Color): Unit = {
    val tessellator = Tessellator.getInstance()
    val bufferBuilder = tessellator.getBufferBuilder

    bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)

    Seq((x2, y1), (x2, y2), (x1, y2), (x1, y1)).foreach { case (i, j) =>
      bufferBuilder.vertex(i, j, 0)
        .color(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
        .next()
    }

    tessellator.draw()
  }

  def getDisplayString(inventory: MassItemInventory): String = if (inventory.isInvEmpty) {
    I18n.translate("stockpile.barrel.empty")
  } else {
    inventory.amountStored.shorthand + (if (inventory.allowNewStackWhenEmpty) {
      "*"
    } else {
      ""
    })
  }
}
