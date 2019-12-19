package me.branchpanic.mods.stockpile.content.client.renderer

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Quaternion

const val COFH_TRANSFORM_OFFSET = 1.0 / 512

// TODO: No longer needed
inline fun transformToPlane(matrixStack: MatrixStack, x: Double, y: Double, z: Double, face: Direction, draw: () -> Unit) {
    matrixStack.push()

    // This current lookup is based on CoFH's Cache rendering table. Many thanks to them for distributing it under
    // a permissive license. I'm not a jerk, I promise.
    when (face) {
        Direction.NORTH ->
            matrixStack.translate(x + 0.75, y + 0.75, z + COFH_TRANSFORM_OFFSET * 145)
        Direction.SOUTH -> {
            matrixStack.translate(x + 0.25, y + 0.75, z + 1 - COFH_TRANSFORM_OFFSET * 145)
            matrixStack.multiply(Quaternion(0.0f, 180.0f, 0.0f, true))
        }
        Direction.WEST -> {
            matrixStack.translate(x + COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.25)
            matrixStack.multiply(Quaternion(0.0f, 90.0f, 0.0f, true))
        }
        Direction.EAST -> {
            matrixStack.translate(x + 1 - COFH_TRANSFORM_OFFSET * 145, y + 0.75, z + 0.75)
            matrixStack.multiply(Quaternion(0.0f, -90.0f, 0.0f, true))
        }
        Direction.UP -> {
            matrixStack.translate(x + 0.75, y + 1 - COFH_TRANSFORM_OFFSET * 145, z + 0.75)
            matrixStack.multiply(Quaternion(90.0f, 0.0f, 0.0f, true))
        }
        Direction.DOWN -> {
            matrixStack.translate(x + 0.75, y + COFH_TRANSFORM_OFFSET * 145, z + 0.25)
            matrixStack.multiply(Quaternion(-90.0f, 0.0f, 0.0f, true))
        }
    }

    draw()

    matrixStack.pop()
}
