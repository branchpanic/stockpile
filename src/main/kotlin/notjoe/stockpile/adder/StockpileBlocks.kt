package notjoe.stockpile.adder

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import notjoe.stockpile.block.BlockBarrel
import org.dimdev.rift.listener.BlockAdder
import org.dimdev.rift.listener.ItemAdder

// This registration system is temporary! It will eventually be dynamic and not hard-coded.
@Suppress("unused")
class StockpileBlocks : BlockAdder, ItemAdder {
    private val standardBarrel = BlockBarrel()

    override fun registerBlocks() {
        Block.registerBlock(ResourceLocation("stockpile", "barrel"), standardBarrel)
    }

    override fun registerItems() {
        Item.registerItemBlock(ItemBlock(standardBarrel, Item.Builder()))
    }
}