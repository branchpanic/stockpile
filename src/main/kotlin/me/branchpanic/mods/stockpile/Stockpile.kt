package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeType
import me.branchpanic.mods.stockpile.block.*
import me.branchpanic.mods.stockpile.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.blockentity.ItemStorageDeviceBlockEntity
import me.branchpanic.mods.stockpile.blockentity.TrashCanBlockEntity
import me.branchpanic.mods.stockpile.item.BarrelHatItem
import me.branchpanic.mods.stockpile.item.BasicUpgradeItem
import me.branchpanic.mods.stockpile.item.UpgradeRemoverItem
import me.branchpanic.mods.stockpile.upgrade.CapacityUpgrade
import me.branchpanic.mods.stockpile.upgrade.MultiplierUpgrade
import me.branchpanic.mods.stockpile.upgrade.TrashUpgrade
import me.branchpanic.mods.stockpile.upgrade.UpgradeInstallerCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Stockpile : ModInitializer {
    private val ITEM_GROUP: ItemGroup = FabricItemGroupBuilder.build(id("all")) { ItemStack(ItemBarrelBlock) }

    internal val BLOCKS: Map<Identifier, Block> = mapOf(
        id("item_barrel") to BarrelBlock.ITEM,
        id("trash_can") to TrashCanBlock,
        id("test_barrel") to StorageDeviceBlock(ItemStorageDeviceBlockEntity.TYPE)
    )

    internal val ITEM_SETTINGS = Item.Settings().group(ITEM_GROUP)
    internal val ITEMS: Map<Identifier, Item> = mapOf(
        id("capacity_upgrade") to BasicUpgradeItem({ CapacityUpgrade(32) }, ITEM_SETTINGS),
        id("double_capacity_upgrade") to BasicUpgradeItem({ CapacityUpgrade(64) }, ITEM_SETTINGS),
        id("multiplier_upgrade") to BasicUpgradeItem({ MultiplierUpgrade(2) }, ITEM_SETTINGS),
        id("double_multiplier_upgrade") to BasicUpgradeItem({ MultiplierUpgrade(4) }, ITEM_SETTINGS),
        id("trash_upgrade") to BasicUpgradeItem({ TrashUpgrade() }, ITEM_SETTINGS),
        id("barrel_hat") to BarrelHatItem,
        id("upgrade_remover") to UpgradeRemoverItem
    )

    internal val BLOCK_ENTITIES: Map<Identifier, BlockEntityType<out BlockEntity>> = mapOf(
        id("item_barrel") to ItemBarrelBlockEntity.TYPE,
        id("trash_can") to TrashCanBlockEntity.TYPE,
        id("test_barrel") to ItemStorageDeviceBlockEntity.TYPE
    )

    internal val UPGRADES: Map<Identifier, UpgradeType> = mapOf(
        id("capacity") to CapacityUpgrade.TYPE,
        id("multiplier") to MultiplierUpgrade.TYPE,
        id("trash") to TrashUpgrade.TYPE
    )

    fun id(path: String): Identifier = Identifier("stockpile", path)

    override fun onInitialize() {
        BLOCKS.forEach { (id, block) ->
            Registry.register(Registry.BLOCK, id, block)

            if (ITEMS.keys.none { itemId -> itemId == id }) {
                Registry.register(Registry.ITEM, id, BlockItem(block, ITEM_SETTINGS.maxDamage(0).maxCount(8)))
            }
        }

        ITEMS.forEach { (id, item) ->
            Registry.register(Registry.ITEM, id, item)
        }

        BLOCK_ENTITIES.forEach { (id, blockEntityType) ->
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, blockEntityType)
        }

        UPGRADES.forEach { (id, upgradeEntry) ->
            UpgradeRegistry.register(id, upgradeEntry)
        }

        UseBlockCallback.EVENT.register(UpgradeInstallerCallback)
        AttackBlockCallback.EVENT.register(AttackableBlockCallback)

        ItemStorage.SIDED.registerForBlockEntity({ ent, _ -> ent.storage }, ItemStorageDeviceBlockEntity.TYPE)

        ServerPlayNetworking.registerGlobalReceiver(id("barrel_hat_restock")) { _: MinecraftServer,
                                                                                player: ServerPlayerEntity,
                                                                                _: ServerPlayNetworkHandler,
                                                                                _: PacketByteBuf,
                                                                                _: PacketSender ->
            if (player.getEquippedStack(EquipmentSlot.HEAD)?.item != BarrelHatItem) return@registerGlobalReceiver

            if (player.isSneaking) {
                BarrelHatItem.pullInventoryFromBarrels(player)
            } else {
                BarrelHatItem.pushInventoryToBarrels(player)
            }
        }
    }
}
