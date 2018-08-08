package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemGroup
import net.minecraft.util.ResourceLocation
import org.dimdev.rift.listener.BlockAdder
import org.dimdev.rift.listener.ItemAdder

@Suppress("unused")
class StockpileBlockAdder : BlockAdder, ItemAdder {
    companion object Definitions {
        val BARREL = BlockBarrel()
        val TRASH_CAN = BlockTrashCan()
    }

    override fun registerBlocks() {
        Block.registerBlock(ResourceLocation("stockpile", "barrel"), BARREL)
        Block.registerBlock(ResourceLocation("stockpile", "trash_can"), TRASH_CAN)
    }

    override fun registerItems() {
        Item.registerItemBlock(ItemBlock(BARREL, Item.Builder().maxStackSize(1).group(ItemGroup.DECORATIONS)))
        Item.registerItemBlock(TRASH_CAN, ItemGroup.DECORATIONS)
    }
}
