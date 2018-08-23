package notjoe.stockpile.util.ext

import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.RayTraceFluidMode
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d

/**
 * Raytraces from a given EntityLiving's eyes.
 * Adapted from https://github.com/MinecraftForge/MinecraftForge/blob/2786cd279cd8feb5965060da27f141d7c8ccf1e5/src/main/java/net/minecraftforge/common/ForgeHooks.java#L1073
 *
 * @param distance Distance to raytrace.
 * @param fluidMode Mode for handling fluids, i.e. specifying whether they should be ignored or not.
 * @return A RayTraceResult providing access to what was hit.
 */
fun EntityLivingBase.rayTraceFromEyes(
        distance: Double,
        fluidMode: RayTraceFluidMode = RayTraceFluidMode.NEVER
): RayTraceResult? {
    val startPos = Vec3d(posX, posY + eyeHeight, posZ)
    val endPos = startPos.add(lookVec.scale(distance))
    return world.rayTraceBlocks(startPos, endPos, fluidMode)
}