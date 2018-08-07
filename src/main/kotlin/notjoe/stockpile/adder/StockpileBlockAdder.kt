package notjoe.stockpile.adder

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.ResourceLocation
import notjoe.stockpile.block.BlockBarrel
import notjoe.stockpile.block.BlockTrashCan
import org.dimdev.rift.listener.BlockAdder
import org.dimdev.rift.listener.ItemAdder

@Suppress("unused")
class StockpileBlockAdder : BlockAdder, ItemAdder {
    private val blocks = mapOf<String, Block>(
            "barrel" to BlockBarrel(32),
            "trash_can" to BlockTrashCan()
    )

    override fun registerBlocks() {
        blocks.forEach { name, block -> Block.registerBlock(ResourceLocation("stockpile", name), block) }
    }

    override fun registerItems() {
        blocks.forEach { _, block -> Item.registerItemBlock(block, ItemGroup.DECORATIONS) }
    }
}
