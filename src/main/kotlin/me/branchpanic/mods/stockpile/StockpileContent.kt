package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.Stockpile.ITEM_SETTINGS
import me.branchpanic.mods.stockpile.block.*
import me.branchpanic.mods.stockpile.blockentity.*
import me.branchpanic.mods.stockpile.item.*
import me.branchpanic.mods.stockpile.upgrade.CapacityUpgrade
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.util.Identifier

@Suppress("MemberVisibilityCanBePrivate")
object StockpileContent {
    object Blocks {
        val TEST_BARREL = StorageDeviceBlock { ItemStorageDeviceBlockEntity.TYPE }
        val TRASH_CAN = TrashCanBlock

        internal val TO_REGISTER = sequence<Pair<String, Block>> {
            yield("test_barrel" to TEST_BARREL)
            yield("trash_can" to TRASH_CAN)
        }
    }

    object Items {
        @JvmField val CAPACITY_UPGRADE = BasicUpgradeItem({ CapacityUpgrade(32) }, ITEM_SETTINGS)

        internal val TO_REGISTER = sequence<Pair<String, Item>> {
            yield("capacity_upgrade" to CAPACITY_UPGRADE)
        }
    }

    object BlockEntityTypes {
        val ALL = sequence<BlockEntityType<*>> { }
    }
}