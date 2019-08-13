package me.branchpanic.mods.stockpile.content.client.renderer

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.util.math.Direction

const val COFH_TRANSFORM_OFFSET = 1.0 / 512

inline fun transformToPlane(x: Double, y: Double, z: Double, face: Direction, draw: () -> Unit) {
    GlStateManager.pushMatrix()

    // TODO: Replace with calculation instead of a table
    // This current lookup is based on CoFH's Cache rendering table. Many thanks to them for distributing it under
    // a permissive license. I'm not a jerk, I promise.
    when (face) {
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

    draw()

    GlStateManager.popMatrix()
}
