package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.content.block.TrashCanBlock
import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.blockentity.TrashCanBlockEntity
import me.branchpanic.mods.stockpile.content.item.BarrelHatItem
import me.branchpanic.mods.stockpile.content.item.BasicUpgradeItem
import me.branchpanic.mods.stockpile.content.upgrade.CapacityUpgrade
import me.branchpanic.mods.stockpile.content.upgrade.MultiplierUpgrade
import me.branchpanic.mods.stockpile.content.upgrade.UpgradeInstaller
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Stockpile : ModInitializer {
    internal val LOGGER: Logger = LogManager.getLogger("stockpile")

    private val ITEM_GROUP: ItemGroup = FabricItemGroupBuilder.build(id("all")) { ItemStack(ItemBarrelBlock) }

    private val BLOCKS: Map<Identifier, Block> = mapOf(
        id("item_barrel") to ItemBarrelBlock,
        id("trash_can") to TrashCanBlock
    )

    internal val ITEM_SETTINGS = Item.Settings().itemGroup(ITEM_GROUP)
    private val ITEMS: Map<Identifier, Item> = mapOf(
        id("capacity_upgrade") to BasicUpgradeItem({ CapacityUpgrade(32) }, ITEM_SETTINGS),
        id("double_capacity_upgrade") to BasicUpgradeItem({ CapacityUpgrade(64) }, ITEM_SETTINGS),
        id("multiplier_upgrade") to BasicUpgradeItem({ MultiplierUpgrade(2) }, ITEM_SETTINGS),
        id("double_multiplier_upgrade") to BasicUpgradeItem({ MultiplierUpgrade(4) }, ITEM_SETTINGS),
        id("barrel_hat") to BarrelHatItem
    )

    private val BLOCK_ENTITIES: Map<Identifier, BlockEntityType<out BlockEntity>> = mapOf(
        id("item_barrel") to ItemBarrelBlockEntity.TYPE,
        id("trash_can") to TrashCanBlockEntity.TYPE
    )

    private val UPGRADES: Map<Identifier, UpgradeType> = mapOf(
        id("capacity") to CapacityUpgrade.TYPE,
        id("multiplier") to MultiplierUpgrade.TYPE
    )

    fun id(path: String): Identifier = Identifier("stockpile", path)

    override fun onInitialize() {
        BLOCKS.forEach { (id, block) ->
            Registry.register(Registry.BLOCK, id, block)

            if (ITEMS.keys.none { itemId -> itemId == id }) {
                Registry.register(Registry.ITEM, id, BlockItem(block, ITEM_SETTINGS))
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

        ServerSidePacketRegistry.INSTANCE.register(id("barrel_hat_restock")) { ctx, _ ->
            if (ctx.player.getEquippedStack(EquipmentSlot.HEAD).item != BarrelHatItem) {
                return@register
            }

            if (ctx.player.isSneaking) {
                BarrelHatItem.pullInventoryFromBarrels(ctx.player)
            } else {
                BarrelHatItem.pushInventoryToBarrels(ctx.player)
            }
        }
    }
}
