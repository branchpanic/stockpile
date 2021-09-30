package me.branchpanic.mods.stockpile.content.client.renderer

import me.branchpanic.mods.stockpile.api.storage.Quantifier
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

class ItemBarrelRenderer : AbstractBarrelRenderer<ItemBarrelBlockEntity, ItemStack>() {

    override fun drawIcon(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        contents: Quantifier<ItemStack>,
        light: Int,
        overlay: Int
    ) {
        matrixStack.translate(0.5, 0.6, 0.01)
        matrixStack.scale(0.48f, 0.48f, 0.05f)

        MinecraftClient.getInstance().itemRenderer.renderItem(
            contents.reference,
            ModelTransformation.Mode.GUI,
            light,
            overlay,
            matrixStack,
            vertexConsumerProvider,
            0
        )
    }

    override fun shouldSkipRenderingFor(barrel: ItemBarrelBlockEntity): Boolean =
        barrel.storage.contents.reference.isEmpty
}
