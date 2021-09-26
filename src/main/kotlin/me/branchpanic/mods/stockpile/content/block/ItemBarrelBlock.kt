package me.branchpanic.mods.stockpile.content.block

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ItemBarrelBlock : BarrelBlock<ItemBarrelBlockEntity>({ pos, state -> ItemBarrelBlockEntity.TYPE.instantiate(pos, state)!! }), AttributeProvider {
    override fun addAllAttributes(world: World?, pos: BlockPos?, state: BlockState?, attributes: AttributeList<*>?) {
        if (world == null || pos == null || state == null || attributes == null) {
            return
        }

        (world.getBlockEntity(pos) as? ItemBarrelBlockEntity)?.let { b -> attributes.offer(b.invAttribute) }
    }
}
