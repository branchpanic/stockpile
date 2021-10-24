package me.branchpanic.mods.stockpile.client.renderer

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.util.math.Matrix4f

fun drawAxes(vcp: VertexConsumerProvider, mat: Matrix4f, x: Float, y: Float, z: Float, size: Float = 1f) {
    val lines = vcp.getBuffer(RenderLayer.getLines())

    // Ugly, but it's just for debugging
    for ((nx, ny, nz) in listOf(Triple(0f, 0f, 1f), Triple(0f, 1f, 0f), Triple(1f, 0f, 0f))) {
        lines.vertex(mat, x, y, z).color(1f, 0f, 0f, 1f).normal(nx, ny, nz).next()
        lines.vertex(mat, x + size, y, z).color(1f, 0f, 0f, 1f).normal(nx, ny, nz).next()
        lines.vertex(mat, x, y, z).color(0f, 1f, 0f, 1f).normal(nx, ny, nz).next()
        lines.vertex(mat, x, y + size, z).color(0f, 1f, 0f, 1f).normal(nx, ny, nz).next()
        lines.vertex(mat, x, y, z).color(0f, 0f, 1f, 1f).normal(nx, ny, nz).next()
        lines.vertex(mat, x, y, z + size).color(0f, 0f, 1f, 1f).normal(nx, ny, nz).next()
    }
}
