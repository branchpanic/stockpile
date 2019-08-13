package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.platform.GlStateManager
import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

@Environment(EnvType.CLIENT)
abstract class AbstractBarrelRenderer<T : AbstractBarrelBlockEntity<U>, U> : BlockEntityRenderer<T>() {
    private val fillBarSettings = FillBarSettings(
        backgroundColor = 0xFF000000.toInt(),
        foregroundColor = 0xFFFF0000.toInt(),
        textColor = 0xFFFFFFFF.toInt(),
        textColorFull = 0xFF00FFFF.toInt(),
        width = 18.0
    )

    abstract fun drawIcon(contents: Quantizer<U>)
    abstract fun shouldSkipRenderingFor(barrel: T): Boolean

    override fun render(
        barrel: T,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        breakStage: Int
    ) {
        super.render(barrel, x, y, z, partialTicks, breakStage)

        val face = barrel.cachedState[Properties.FACING]
        val obscuringPos = barrel.pos.offset(face)
        val state = world.getBlockState(obscuringPos)

        if (state.isFullOpaque(world, obscuringPos) || shouldSkipRenderingFor(barrel)) return

        renderDisplay(barrel, face, x, y, z)
    }

    private fun renderDisplay(
        barrel: T,
        orientation: Direction,
        x: Double,
        y: Double,
        z: Double
    ) {
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

        transformToPlane(x, y, z, orientation) {
            GlStateManager.scaled(0.03125, 0.03125, -COFH_TRANSFORM_OFFSET)
            GlStateManager.rotated(180.0, 0.0, 0.0, 1.0)
            GlStateManager.translated(0.0, 0.0, 6.0)

            drawIcon(barrel.storage.contents)

            GlStateManager.translated(0.0, 0.0, -6.0)
            renderFillBar(
                fillBarSettings,
                fontRenderer,
                barrel.storage.contents.amount,
                barrel.storage.capacity,
                barrel.clearWhenEmpty,
                xCenter = 8.0,
                yCenter = 16.0
            )
        }

        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
    }
}
