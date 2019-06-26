package me.branchpanic.mods.stockpile.content.client.gui

import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface OverlayRenderer {
    fun draw(world: World, heldItem: ItemStack, selectedPos: BlockPos)
}
