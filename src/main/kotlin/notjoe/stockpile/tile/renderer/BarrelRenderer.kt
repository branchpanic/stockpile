package notjoe.stockpile.tile.renderer

import net.minecraft.block.BlockDirectional
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.math.BlockPos
import notjoe.stockpile.tile.TileBarrel
import org.lwjgl.opengl.GL11

const val RENDER_OFFSET = 1.0 / 512.0

class BarrelRenderer : TileEntityRenderer<TileBarrel>() {
    private val renderItem = Minecraft.getMinecraft().renderItem

    @Suppress("FunctionName")
    override fun func_199341_a(tile: TileBarrel?, xPos: Double, yPos: Double, zPos: Double, partialTicks: Float,
                               destroyStage: Int) {
        if (tile == null) {
            return
        }

        val containedItem = tile.stackType
        if (containedItem.isEmpty) {
            println("Empty stack!: " + containedItem.item.name.unformattedComponentText)
            return
        }

        val barrelFrontDirection = tile.blockState.getValue(BlockDirectional.FACING)

        GlStateManager.pushMatrix()
        renderFlatItem(containedItem, tile.pos, barrelFrontDirection, xPos, yPos, zPos)
        GlStateManager.popMatrix()
    }


    // Adapted from https://github.com/CoFH/CoFHCore/blob/f53327609aa6fc6fd3dedbd50a9b763d764bd450/src/main/java/cofh/core/render/RenderUtils.java#L43
    private fun renderFlatItem(stack: ItemStack, tilePos: BlockPos, side: EnumFacing, xPos: Double, yPos: Double, zPos: Double) {
        if (stack.isEmpty) {
            return
        }

        GlStateManager.pushMatrix()

        // If only I was good at linear algebra...
        when (side) {
            NORTH -> {
                GlStateManager.translate(xPos + 0.75, yPos + 0.75, zPos + RENDER_OFFSET * 145)
            }
            SOUTH -> {
                GlStateManager.translate(xPos + 0.25, yPos + 0.75, zPos + 1 - RENDER_OFFSET * 145)
                GlStateManager.rotate(180f, 0f, 1f, 0f)
            }
            EAST -> {
                GlStateManager.translate(xPos + RENDER_OFFSET * 145, yPos + 0.75, zPos + 0.25)
                GlStateManager.rotate(90f, 0f, 1f, 0f)
            }
            WEST -> {
                GlStateManager.translate(xPos + 1 - RENDER_OFFSET * 145, yPos + 0.75, zPos + 0.75)
                GlStateManager.rotate(-90f, 0f, 1f, 0f)
            }
            UP -> {
                GlStateManager.translate(xPos + 0.75, yPos + 1 - RENDER_OFFSET * 145, zPos + 0.75)
                GlStateManager.rotate(90f, 1f, 0f, 0f)
            }
            DOWN -> {
                GlStateManager.translate(xPos + 0.75, yPos + RENDER_OFFSET * 145, zPos + 0.25)
                GlStateManager.rotate(-90f, 1f, 0f, 0f)
            }
        }

        GlStateManager.scale(0.03125, 0.03125, -RENDER_OFFSET)
        GlStateManager.rotate(180f, 0f, 0f, 1f)

        val renderSide = tilePos.offset(side)
        if (!world.getBlockState(renderSide).isFullCube) {
            val lightmapCombined = world.getCombinedLight(renderSide, 3)
            val lightmapU = lightmapCombined % 65536
            val lightmapV = lightmapCombined / 65536
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapU.toFloat(), lightmapV.toFloat())
        }

        renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)

        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.popMatrix()

        RenderHelper.enableStandardItemLighting()
    }
}