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
        @JvmField val BARREL = BlockBarrel()
        @JvmField val TRASH_CAN = BlockTrashCan()
    }

    override fun registerBlocks() {
        Block.register(ResourceLocation("stockpile", "barrel"), BARREL)
        Block.register(ResourceLocation("stockpile", "trash_can"), TRASH_CAN)
    }

    override fun registerItems() {
        Item.registerItemBlock(ItemBlock(BARREL, Item.Builder().maxStackSize(1).group(ItemGroup.DECORATIONS)))
        Item.registerItemBlock(TRASH_CAN, ItemGroup.DECORATIONS)
    }
}
