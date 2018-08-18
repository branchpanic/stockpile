package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemGroup
import net.minecraft.util.ResourceLocation
import org.dimdev.rift.listener.BlockAdder
import org.dimdev.rift.listener.ItemAdder
import kotlin.properties.Delegates

@Suppress("unused")
class StockpileBlocks : BlockAdder, ItemAdder {
    companion object Definitions {
        val barrel = BlockBarrel()
        val trashCan = BlockTrashCan()
        val gratedHopper = BlockGratedHopper()
    }

    override fun registerBlocks() {
        Block.register(ResourceLocation("stockpile", "barrel"), barrel)
        Block.register(ResourceLocation("stockpile", "trash_can"), trashCan)
        Block.register(ResourceLocation("stockpile", "grated_hopper"), gratedHopper)
    }

    override fun registerItems() {
        Item.registerItemBlock(ItemBlock(barrel, Item.Builder().maxStackSize(1).group(ItemGroup.DECORATIONS)))
        Item.registerItemBlock(trashCan, ItemGroup.DECORATIONS)
        Item.registerItemBlock(gratedHopper, ItemGroup.REDSTONE)
    }
}
