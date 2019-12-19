package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Quaternion

@Environment(EnvType.CLIENT)
abstract class AbstractBarrelRenderer<T : AbstractBarrelBlockEntity<U>, U>(dispatcher: BlockEntityRenderDispatcher) :
    BlockEntityRenderer<T>(dispatcher) {

    private val fillBarSettings = FillBarSettings(
        backgroundColor = 0xB2000000.toInt(),
        foregroundColor = 0xB20212FF.toInt(),
        textColor = 0xFFFFFFFF.toInt(),
        textColorFull = 0xFFFFFF22.toInt(),
        width = 18.0
    )

    abstract fun shouldSkipRenderingFor(barrel: T): Boolean

    override fun render(
        barrel: T,
        f: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        j: Int
    ) {
        val face = barrel.cachedState[Properties.FACING]
        val pos = barrel.pos
        val obscuringPos = pos.offset(face)
        val world = barrel.world
        val state = world?.getBlockState(obscuringPos) ?: return

        if (state.isFullOpaque(world, obscuringPos) || shouldSkipRenderingFor(barrel)) return

        matrixStack.push()
        renderDisplay(barrel, face, matrixStack, vertexConsumerProvider, i, j)
    }


    private fun renderDisplay(
        barrel: T,
        orientation: Direction,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        j: Int
    ) {
        transformToPlane(matrixStack, 0.0, 0.0, 0.0, orientation) {
            matrixStack.scale(0.03125f, 0.03125f, (-COFH_TRANSFORM_OFFSET).toFloat())
            matrixStack.multiply(Quaternion(0.0f, 0.0f, 180.0f, true))
            matrixStack.translate(0.0, 0.0, 6.0)

            drawIcon(matrixStack, vertexConsumerProvider, barrel.storage.contents, i, j)

            matrixStack.translate(0.0, 0.0, -6.0)
            renderFillBar(
                matrixStack,
                vertexConsumerProvider,
                fillBarSettings,
                dispatcher.textRenderer,
                barrel.storage.contents.amount,
                barrel.storage.capacity,
                barrel.clearWhenEmpty,
                xCenter = 8.0,
                yCenter = 16.0,
                i=i, j=j
            )
        }

        matrixStack.pop()
    }

    abstract fun drawIcon(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        contents: Quantizer<U>,
        i: Int,
        j: Int
    )

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
