package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemGroup
import net.minecraft.util.ResourceLocation
import org.dimdev.rift.listener.BlockAdder
import org.dimdev.rift.listener.ItemAdder

@Suppress("unused")
class StockpileBlocks : BlockAdder, ItemAdder {
    companion object Definitions {
        val barrel = BlockBarrel()
        val trashCan = BlockTrashCan()
    }

    override fun registerBlocks() {
        Block.registerBlock(ResourceLocation("stockpile", "barrel"), barrel)
        Block.registerBlock(ResourceLocation("stockpile", "trash_can"), trashCan)
    }

    override fun registerItems() {
        Item.registerItemBlock(ItemBlock(barrel, Item.Builder().maxStackSize(1).group(ItemGroup.DECORATIONS)))
        Item.registerItemBlock(trashCan, ItemGroup.DECORATIONS)
    }
}
