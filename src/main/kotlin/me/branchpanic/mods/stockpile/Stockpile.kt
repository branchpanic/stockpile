package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import net.fabricmc.api.ModInitializer
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.item.block.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Stockpile : ModInitializer {
    
    private val BLOCKS: Map<Identifier, Block> = mapOf(
        id("item_barrel") to ItemBarrelBlock
    )

    private val BLOCK_ENTITIES: Map<Identifier, BlockEntityType<out BlockEntity>> = mapOf(
        id("item_barrel") to ItemBarrelBlockEntity.TYPE
    )

    val LOGGER: Logger = LogManager.getLogger("stockpile")

    private fun id(path: String): Identifier = Identifier("stockpile", path)

    override fun onInitialize() {
        BLOCKS.forEach { id, block ->
            Registry.register(Registry.BLOCK, id, block)
            Registry.register(Registry.ITEM, id, BlockItem(block, Item.Settings()))
        }

        BLOCK_ENTITIES.forEach { id, blockEntityType ->
            Registry.register(Registry.BLOCK_ENTITY, id, blockEntityType)
        }
    }
}
