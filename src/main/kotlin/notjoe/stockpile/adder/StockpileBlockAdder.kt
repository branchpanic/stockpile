package notjoe.stockpile.adder

import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation
import notjoe.stockpile.block.BlockBarrel
import org.dimdev.rift.listener.BlockAdder

class StockpileBlockAdder : BlockAdder {
    override fun registerBlocks() {
        Block.registerBlock(ResourceLocation("stockpile", "barrel"), BlockBarrel())
    }
}