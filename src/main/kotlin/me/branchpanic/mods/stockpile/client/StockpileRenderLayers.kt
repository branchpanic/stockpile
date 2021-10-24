package me.branchpanic.mods.stockpile.client

import net.minecraft.client.render.*

object StockpileRenderLayers : RenderPhase(null, null, null) {
    @JvmField
    val STORAGE_DEVICE: RenderLayer = RenderLayer.of(
        "stockpile:storage_device",
        VertexFormats.POSITION_COLOR_LIGHT,
        VertexFormat.DrawMode.QUADS,
        128,
        false,
        false,
        RenderLayer.MultiPhaseParameters.builder()
            .shader(Shader(GameRenderer::getPositionColorLightmapShader))
            .lightmap(RenderLayer.ENABLE_LIGHTMAP)
            .build(false)
    )
}
