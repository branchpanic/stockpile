package me.branchpanic.mods.stockpile.client.renderer

import me.branchpanic.mods.stockpile.api.StorageDeviceBlockEntity
import me.branchpanic.mods.stockpile.client.StockpileRenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.*

// Arbitrary small value to avoid z-fighting
private const val EPSILON = 1e-3f

abstract class StorageDeviceRenderer<T : StorageDeviceBlockEntity>(protected val context: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<T> {
    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true

    override fun render(
        blockEntity: T,
        time: Float,
        matrixStack: MatrixStack?,
        vertexConsumerProvider: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        if (matrixStack == null) return
        if (vertexConsumerProvider == null) return

        val face = blockEntity.cachedState.getOrEmpty(Properties.FACING).orElse(Direction.NORTH)
        val world = blockEntity.world

        val obscuringPos = blockEntity.pos.offset(face)
        val obscuringState = world?.getBlockState(obscuringPos) ?: return
        if (obscuringState.isOpaqueFullCube(world, obscuringPos)) return

        matrixStack.push()

        // Set up a GUI-like coordinate system:
        // - (0, 0, 0) is top-left
        // - (16, 16, 0) is bottom-right
        // - +X is right
        // - +Y is down
        // - +Z is out
        matrixStack.translate(0.5, 0.5, 0.5)
        matrixStack.multiply(face.rotationQuaternion)
        matrixStack.multiply(Quaternion(-90f, 0f, 0f, true))
        matrixStack.scale(1f, -1f, 1f)
        matrixStack.translate(-0.5, -0.5, 0.5 + EPSILON)
        matrixStack.scale(1 / 16f, 1 / 16f, 1 / 16f)

        // TODO: Remove when done debugging
        drawAxes(vertexConsumerProvider, matrixStack.peek().model, 0f, 0f, 0f)

        vertexConsumerProvider.drawFillBar(
            matrixStack,
            0f, 0f,
            16f, 16f,
            EPSILON,
            10,
            blockEntity.amount.toFloat() / blockEntity.capacity,
            blockEntity.amount.toString()
        )

        // TODO: Give this the block entity and a suggested region to draw inside of
        drawContents(matrixStack, vertexConsumerProvider, blockEntity, Vec3f.ZERO, Vec3f.ZERO)

        matrixStack.pop()
    }

    abstract fun drawContents(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        blockEntity: T,
        min: Vec3f,
        max: Vec3f
    )

    protected fun VertexConsumer.drawQuad(
        mat: Matrix4f,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        z: Float,
        color: IntColor,
        light: Int
    ) {
        vertex(mat, x0, y0, z).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        vertex(mat, x0, y1, z).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        vertex(mat, x1, y1, z).color(color.red, color.green, color.blue, color.alpha).light(light).next()
        vertex(mat, x1, y0, z).color(color.red, color.green, color.blue, color.alpha).light(light).next()
    }

    // FIXME: This has a lot of arguments
    //  Positioning will probably be moved to static configuration
    protected fun VertexConsumerProvider.drawFillBar(
        matrixStack: MatrixStack,
        topLeftX: Float,
        topLeftY: Float,
        width: Float,
        height: Float,
        z: Float,
        light: Int,
        normalizedValue: Float,
        text: String?,
    ) {
        val mat = matrixStack.peek().model
        val storageDeviceBuffer = getBuffer(StockpileRenderLayers.STORAGE_DEVICE)

        if (normalizedValue > 0) {
            storageDeviceBuffer.drawQuad(
                mat,
                topLeftX,
                topLeftY,
                topLeftX + normalizedValue * width,
                topLeftY + height,
                z,
                DEFAULT_BLUE,
                light,
            )
        }

        if (normalizedValue < 1) {
            storageDeviceBuffer.drawQuad(
                mat,
                topLeftX + normalizedValue * width,
                topLeftY,
                topLeftX + width,
                topLeftY + height,
                z,
                DEFAULT_GRAY,
                light
            )
        }

        matrixStack.push()
        matrixStack.translate(0.0, 0.0, 2 * EPSILON.toDouble())
        matrixStack.scale(1 / 4f, 1 / 4f, 1 / 4f)
        // TODO: Position text
        context.textRenderer.draw(matrixStack, text, 0f, 0f, 0xFFFFFFFF.toInt())
        matrixStack.pop()
    }
}