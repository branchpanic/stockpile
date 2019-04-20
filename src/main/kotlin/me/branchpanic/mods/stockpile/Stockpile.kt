package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.item.BasicUpgradeItem
import me.branchpanic.mods.stockpile.content.upgrade.CapacityUpgrade
import me.branchpanic.mods.stockpile.content.upgrade.UpgradeInstaller
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Stockpile : ModInitializer {
    internal val LOGGER: Logger = LogManager.getLogger("stockpile")

    private val BLOCKS: Map<Identifier, Block> = mapOf(
        id("item_barrel") to ItemBarrelBlock
    )

    private val ITEMS: Map<Identifier, Item> = mapOf(
        id("capacity_upgrade") to BasicUpgradeItem({ CapacityUpgrade(32) }, Item.Settings())
    )

    private val BLOCK_ENTITIES: Map<Identifier, BlockEntityType<out BlockEntity>> = mapOf(
        id("item_barrel") to ItemBarrelBlockEntity.TYPE
    )

    private val UPGRADES: Map<Identifier, UpgradeType> = mapOf(
        id("capacity") to CapacityUpgrade.TYPE
    )

    fun id(path: String): Identifier = Identifier("stockpile", path)

    override fun onInitialize() {
        BLOCKS.forEach { (id, block) ->
            Registry.register(Registry.BLOCK, id, block)

            if (ITEMS.keys.none { itemId -> itemId == id }) {
                Registry.register(Registry.ITEM, id, BlockItem(block, Item.Settings()))
            }
        }

        ITEMS.forEach { (id, item) ->
            Registry.register(Registry.ITEM, id, item)
        }

        BLOCK_ENTITIES.forEach { (id, blockEntityType) ->
            Registry.register(Registry.BLOCK_ENTITY, id, blockEntityType)
        }

        UPGRADES.forEach { (id, upgradeEntry) ->
            UpgradeRegistry.register(id, upgradeEntry)
        }

        UseBlockCallback.EVENT.register(UpgradeInstaller)
    }
}
