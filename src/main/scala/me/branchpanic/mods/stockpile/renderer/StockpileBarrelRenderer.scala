package me.branchpanic.mods.stockpile.renderer

import java.awt.Color

import com.mojang.blaze3d.platform.GlStateManager
import me.branchpanic.mods.stockpile.blockentity.StockpileBarrelBlockEntity
import me.branchpanic.mods.stockpile.extension.IntExtensions._
import me.branchpanic.mods.stockpile.inventory.MassItemInventory
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.{Tessellator, VertexFormats}
import net.minecraft.client.resource.language.I18n
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import org.lwjgl.opengl.GL11

@Environment(EnvType.CLIENT)
object StockpileBarrelRenderer extends BlockEntityRenderer[StockpileBarrelBlockEntity] {
  val FILL_BAR_WIDTH = 18.0
  val COFH_TRANSFORM_OFFSET: Double = 1.0 / 512.0

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

    if (getWorld.getBlockState(barrel.getPos.offset(frontFaceDirection)).isFullOpaque(getWorld, barrel.getPos)) {
      return
    }

    renderBarrelDisplay(barrel, frontFaceDirection, x, y, z)
  }

  def transformToFace(orientation: Direction, x: Double, y: Double, z: Double): Unit =
    orientation match {
      case Direction.NORTH =>
        GlStateManager.translated(x + 0.75, y + 0.75, z + COFH_TRANSFORM_OFFSET * 145)
      case Direction.SOUTH =>
        GlStateManager.translated(x + 0.25, y + 0.75, z + 1 - COFH_TRANSFORM_OFFSET * 145)
        GlStateManager.rotated(180f, 0f, 1f, 0f)
      case Direction.WEST =>
        GlStateManager.translated(x + COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.25)
        GlStateManager.rotated(90f, 0f, 1f, 0f)
      case Direction.EAST =>
        GlStateManager.translated(x + 1 - COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.75)
        GlStateManager.rotated(-90f, 0f, 1f, 0f)
      case Direction.UP =>
        GlStateManager.translated(x + 0.75, y + 1 - COFH_TRANSFORM_OFFSET * 145, z + 0.75)
        GlStateManager.rotated(90f, 1f, 0f, 0f)
      case Direction.DOWN =>
        GlStateManager.translated(x + 0.75, y + COFH_TRANSFORM_OFFSET * 145, z + 0.25)
        GlStateManager.rotated(-90f, 1f, 0f, 0f)
    }

  def renderFillBar(inventory: MassItemInventory, xCenter: Float, yCenter: Float): Unit = {
    GlStateManager.translated(0.0, 0.0, 0.3 * 1 / COFH_TRANSFORM_OFFSET)
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

    val totalBarWidth = 2 * FILL_BAR_WIDTH + 0.25 * FILL_BAR_WIDTH
    val filledBarWidth = totalBarWidth * Math.min(filledAmount, 1.0)
    val unfilledBarWidth = totalBarWidth - filledBarWidth

    GlStateManager.disableTexture()

    if (filledBarWidth > 0) {
      drawRectangle(
        -0.25 * FILL_BAR_WIDTH,
        textCenterY + textHeight,
        filledBarWidth - 0.25 * FILL_BAR_WIDTH,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 1f, 0.7f)
      )
    }

    if (unfilledBarWidth > 0) {
      drawRectangle(
        filledBarWidth - 0.25 * FILL_BAR_WIDTH,
        textCenterY + textHeight,
        filledBarWidth + unfilledBarWidth - 0.25 * FILL_BAR_WIDTH,
        textCenterY - 0.25 * textHeight,
        new Color(0f, 0f, 0f, 0.7f)
      )
    }

    GlStateManager.enableTexture()

    GlStateManager.translated(0.0, 0.0, 0.02)
    getFontRenderer.draw(displayText, textCenterX, textCenterY, textColor)
  }

  def renderBarrelDisplay(barrel: StockpileBarrelBlockEntity,
                          orientation: Direction,
                          x: Double,
                          y: Double,
                          z: Double): Unit = {
    val stack = barrel.inventory.stackType

    GlStateManager.pushMatrix()

    transformToFace(orientation, x, y, z)

    GlStateManager.scaled(0.03125, 0.03125, -COFH_TRANSFORM_OFFSET)
    GlStateManager.rotated(180, 0, 0, 1)

    MinecraftClient.getInstance().getItemRenderer.renderGuiItem(stack, 0, -3)
    renderFillBar(barrel.inventory, 8f, 16f)

    GlStateManager.popMatrix()
  }

  def drawRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: Color): Unit = {
    val tessellator = Tessellator.getInstance()
    val bufferBuilder = tessellator.getBufferBuilder

    bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)

    Seq((x2, y1), (x2, y2), (x1, y2), (x1, y1)).foreach {
      case (i, j) =>
        bufferBuilder
          .vertex(i, j, 0)
          .color(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
          .next()
    }

    tessellator.draw()
  }

  def getDisplayString(inventory: MassItemInventory): String =
    if (inventory.isInvEmpty) {
      I18n.translate("stockpile.barrel.empty")
    } else {
      inventory.amountStored.shorthand + (if (inventory.isAcceptingNewStackWhenEmpty) {
                                            "*"
                                          } else {
                                            ""
                                          })
    }
}
