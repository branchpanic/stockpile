package me.branchpanic.mods.stockpile.client.renderer

import me.branchpanic.mods.stockpile.blockentity.ItemStorageDeviceBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3f

class ItemStorageDeviceRenderer(context: BlockEntityRendererFactory.Context) :
    StorageDeviceRenderer<ItemStorageDeviceBlockEntity>(context) {
    override fun drawContents(
        matrixStack: MatrixStack,
        blockEntity: VertexConsumerProvider?,
        min: Vec3f,
        max: Vec3f
    ) {
        // TODO: Draw a flat item
    }
}