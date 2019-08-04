package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.platform.GlStateManager
import me.branchpanic.mods.stockpile.api.storage.MassStorage
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.resource.language.I18n
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

// 2 rewrites in and we're still using the same awful rendering code.
// I'm not even sure if I'm sorry at this point.

@Environment(EnvType.CLIENT)
object ItemBarrelRenderer : BlockEntityRenderer<ItemBarrelBlockEntity>() {
    private const val FILL_BAR_WIDTH = 18.0
    private const val COFH_TRANSFORM_OFFSET = 1.0 / 512

    override fun render(
        barrel: ItemBarrelBlockEntity?,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        breakStage: Int
    ) {
        super.render(barrel, x, y, z, partialTicks, breakStage)

        if (barrel == null) return

        val face = barrel.cachedState[Properties.FACING]
        val obscuringPos = barrel.pos.offset(face)

        if (world.getBlockState(obscuringPos).isFullOpaque(
                world,
                obscuringPos
            ) || barrel.storage.isEmpty
        ) return

        renderDisplay(barrel.storage, face, x, y, z)
    }

    private fun transformToFace(orientation: Direction, x: Double, y: Double, z: Double) = when (orientation) {
        Direction.NORTH ->
            GlStateManager.translated(x + 0.75, y + 0.75, z + COFH_TRANSFORM_OFFSET * 145)
        Direction.SOUTH -> {
            GlStateManager.translated(x + 0.25, y + 0.75, z + 1 - COFH_TRANSFORM_OFFSET * 145)
            GlStateManager.rotated(180.0, 0.0, 1.0, 0.0)
        }
        Direction.WEST -> {
            GlStateManager.translated(x + COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.25)
            GlStateManager.rotated(90.0, 0.0, 1.0, 0.0)
        }
        Direction.EAST -> {
            GlStateManager.translated(x + 1 - COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.75)
            GlStateManager.rotated(-90.0, 0.0, 1.0, 0.0)
        }
        Direction.UP -> {
            GlStateManager.translated(x + 0.75, y + 1 - COFH_TRANSFORM_OFFSET * 145, z + 0.75)
            GlStateManager.rotated(90.0, 1.0, 0.0, 0.0)
        }
        Direction.DOWN -> {
            GlStateManager.translated(x + 0.75, y + COFH_TRANSFORM_OFFSET * 145, z + 0.25)
            GlStateManager.rotated(-90.0, 1.0, 0.0, 0.0)
        }
        else -> {
        }
    }

    private fun renderFillBar(storage: MassStorage<ItemStack>, xCenter: Double, yCenter: Double) {
        GlStateManager.translated(0.0, 0.0, 0.3 * 1 / COFH_TRANSFORM_OFFSET)
        GlStateManager.scaled(0.5, 0.5, 1.0)

        val displayText = getDisplayText(storage)
        val amount = storage.contents.amount
        val capacity = storage.capacity
        val filledAmount = amount.toDouble() / capacity
        val textColor = if (capacity > amount) {
            0xFFFFFF
        } else {
            0xFFFF22
        }

        val textWidth = fontRenderer.getStringWidth(displayText)
        val textHeight = fontRenderer.fontHeight
        val textCenterX = xCenter * 2 - (textWidth / 2)
        val textCenterY = yCenter * 2 - (textHeight / 2)

        val totalBarWidth = 2 * FILL_BAR_WIDTH + 0.25 * FILL_BAR_WIDTH
        val filledBarWidth = totalBarWidth * min(filledAmount, 1.0)
        val unfilledBarWidth = totalBarWidth - filledBarWidth

        GlStateManager.disableTexture()

        if (filledBarWidth > 0) {
            drawRectangle(
                -0.25 * FILL_BAR_WIDTH,
                textCenterY + textHeight,
                filledBarWidth - 0.25 * FILL_BAR_WIDTH,
                textCenterY - 0.25 * textHeight,
                Color(0f, 0f, 1f, 0.7f)
            )
        }

        if (unfilledBarWidth > 0) {
            drawRectangle(
                filledBarWidth - 0.25 * FILL_BAR_WIDTH,
                textCenterY + textHeight,
                filledBarWidth + unfilledBarWidth - 0.25 * FILL_BAR_WIDTH,
                textCenterY - 0.25 * textHeight,
                Color(0f, 0f, 0f, 0.7f)
            )
        }

        GlStateManager.enableTexture()

        GlStateManager.translated(0.0, 0.0, 0.02)
        fontRenderer.draw(displayText, textCenterX.toFloat(), textCenterY.toFloat(), textColor)
    }

    private fun drawRectangle(x1: Double, y1: Double, x2: Double, y2: Double, color: Color) {
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.bufferBuilder

        bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR)
        bufferBuilder.vertex(x2, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
        bufferBuilder.vertex(x2, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
        bufferBuilder.vertex(x1, y2, 0.0).color(color.red, color.green, color.blue, color.alpha).next()
        bufferBuilder.vertex(x1, y1, 0.0).color(color.red, color.green, color.blue, color.alpha).next()

        tessellator.draw()
    }

    private fun renderDisplay(
        storage: MassStorage<ItemStack>,
        orientation: Direction,
        x: Double,
        y: Double,
        z: Double
    ) {
        val stack = storage.contents.reference

        GlStateManager.enableRescaleNormal()
        GlStateManager.alphaFunc(516, 0.1F)
        GlStateManager.enableBlend()
        GuiLighting.enable()
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.pushMatrix()

        transformToFace(orientation, x, y, z)

        GlStateManager.scaled(0.03125, 0.03125, -COFH_TRANSFORM_OFFSET)
        GlStateManager.rotated(180.0, 0.0, 0.0, 1.0)
        GlStateManager.translated(0.0, 0.0, 6.0)

        MinecraftClient.getInstance().itemRenderer.renderGuiItem(stack, 0, -3)

        GlStateManager.translated(0.0, 0.0, -6.0)
        renderFillBar(storage, 8.0, 16.0)

        GlStateManager.popMatrix()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
    }

    private fun getDisplayText(storage: MassStorage<ItemStack>): String {
        return if (storage.isEmpty) {
            I18n.translate("ui.stockpile.empty")
        } else {
            // TODO: Change on clear when empty
            storage.contents.amount.abbreviate() + if (true) "*" else ""
        }
    }
}

val INT_SUFFIXES = arrayOf("k", "M", "B", "T", "Q")

fun Long.abbreviate(): String {
    val orderOfMagnitude = log10(abs(toDouble())).toInt()
    if (orderOfMagnitude < 4) {
        return "%,d".format(this)
    }

    val displayMagnitude = orderOfMagnitude / 3
    val suffix = INT_SUFFIXES[min(displayMagnitude - 1, INT_SUFFIXES.size)]
    val displayNumber = "%.1f".format(this / 10.0.pow(3 * displayMagnitude))

    return displayNumber + suffix
}