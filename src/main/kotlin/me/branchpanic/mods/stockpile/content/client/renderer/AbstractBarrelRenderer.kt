package me.branchpanic.mods.stockpile.content.client.renderer

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.storage.Quantifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.Matrix4f
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Quaternion

@Environment(EnvType.CLIENT)
abstract class AbstractBarrelRenderer<T : AbstractBarrelBlockEntity<U>, U>(dispatcher: BlockEntityRenderDispatcher) :
    BlockEntityRenderer<T>(dispatcher) {

    private val fillBarSettings = FillBarSettings(
        backgroundColor = 0xB20A0A0A.toInt(),
        foregroundColor = 0xB20212FF.toInt(),
        textColor = 0xFFFFFFFF.toInt(),
        textColorFull = 0xFFFFFF22.toInt(),
        width = 18.0
    )

    abstract fun shouldSkipRenderingFor(barrel: T): Boolean
    abstract fun drawIcon(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        contents: Quantifier<U>,
        light: Int,
        overlay: Int
    )

    override fun render(
        barrel: T,
        tickDelta: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val face = barrel.cachedState[Properties.FACING]
        val world = barrel.world
        val obscuringPos = barrel.pos.offset(face)
        val obscuringState = world?.getBlockState(obscuringPos) ?: return

        if (obscuringState.isFullOpaque(world, obscuringPos) || shouldSkipRenderingFor(barrel)) return

        renderDisplay(
            barrel,
            face,
            matrixStack,
            vertexConsumerProvider,
            0xF000F0 /* `light` param is always 0? */,
            overlay
        )
    }

    private fun renderDisplay(
        barrel: T,
        orientation: Direction,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrixStack.push()

        // WIP, will make part of FillBarSettings
        val padding = 0.18
        val barWidth = 1.0f - 2 * padding.toFloat()
        val barHeight = dispatcher.textRenderer.fontHeight / 64f + 0.02f

        // Translate to be coplanar to barrel with (0, 0, 0) being bottom-left corner.
        matrixStack.translate(0.5, 0.5, 0.5)
        matrixStack.multiply(orientation.rotationQuaternion)
        matrixStack.multiply(Quaternion(-90f, 0f, 0f, true))
        matrixStack.translate(-0.5, -0.5, 0.51)

        // Draw barrel-specific icon.
        matrixStack.push()
        drawIcon(matrixStack, vertexConsumerProvider, barrel.storage.contents, light, overlay)
        matrixStack.pop()

        // Draw fill bar.
        matrixStack.push()
        matrixStack.translate(padding, padding, 0.0)
        val buf = vertexConsumerProvider.getBuffer(RenderLayer.getLeash())
        val mx = matrixStack.peek().model
        val fillAmount = barrel.storage.contents.amount.toFloat() / barrel.storage.capacity
        val filledWidth = barWidth * fillAmount
        buf.rect(mx, 0f, 0f, filledWidth, barHeight, fillBarSettings.foregroundColor, light)
        buf.rect(mx, filledWidth, 0f, barWidth, barHeight, fillBarSettings.backgroundColor, light)
        matrixStack.pop()

        // Draw text.
        val textWidth = dispatcher.textRenderer.getStringWidth(barrel.fillBarText)
        matrixStack.push()
        matrixStack.translate(0.0, padding, 0.01)
        matrixStack.scale(1 / 64f, -1 / 64f, -1f)
        matrixStack.translate(
            (64 - textWidth.toDouble()) / 2.0,
            (-dispatcher.textRenderer.fontHeight).toDouble(),
            0.0
        )
        dispatcher.textRenderer.draw(
            barrel.fillBarText, 0f, 0f, when {
                barrel.storage.isFull -> fillBarSettings.textColorFull
                else -> fillBarSettings.textColor
            }, false, matrixStack.peek().model, vertexConsumerProvider, false, 0, light
        )
        matrixStack.pop()

        matrixStack.pop()
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true

    private val T.fillBarText: String
        get() = if ((storage.contents.amount / storage.capacity.toDouble()) <= 0) {
            I18n.translate("ui.stockpile.empty")
        } else {
            storage.contents.amount.abbreviate() + if (clearWhenEmpty) "*" else ""
        }

    private fun VertexConsumer.rect(
        mx: Matrix4f,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: ArgbColor,
        light: Int
    ) {
        this.vertex(mx, x2, y1, 0f).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        this.vertex(mx, x2, y2, 0f).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        this.vertex(mx, x1, y2, 0f).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        this.vertex(mx, x1, y1, 0f).color(color.red, color.green, color.blue, color.alpha).light(light).next()
    }
}

