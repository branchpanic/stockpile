package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.BarrelTransactionAmount
import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.content.upgrade.TrashUpgrade
import me.branchpanic.mods.stockpile.extension.giveTo
import me.branchpanic.mods.stockpile.impl.attribute.FixedMassItemInv
import me.branchpanic.mods.stockpile.impl.attribute.UnrestrictedInventoryFixedWrapper
import me.branchpanic.mods.stockpile.impl.storage.*
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

class ItemBarrelBlockEntity(
    internal var clearWhenEmpty: Boolean = true,
    override var appliedUpgrades: List<Upgrade> = emptyList(),
    override var maxUpgrades: Int = DEFAULT_MAX_UPGRADES
) : AbstractBarrelBlockEntity<ItemStack>(
    storage = MassItemStackStorage(ItemStackQuantizer.NONE, DEFAULT_CAPACITY_STACKS),
    doubleClickThresholdMs = 1000,
    type = TYPE
), BlockEntityClientSerializable, Inventory, UpgradeContainer {

    override fun isUpgradeTypeAllowed(u: Upgrade): Boolean = u is ItemBarrelUpgrade

    override fun pushUpgrade(u: Upgrade) {
        appliedUpgrades += u
        markDirty()
    }

    override fun popUpgrade(): Upgrade {
        val result = appliedUpgrades.last()
        markDirty()
        appliedUpgrades = appliedUpgrades.dropLast(1)
        return result
    }

    companion object {
        const val DEFAULT_CAPACITY_STACKS = 32
        const val DEFAULT_MAX_UPGRADES = 6

        const val STORED_ITEM_TAG = "StoredItem"
        const val AMOUNT_STORED_TAG = "AmountStored"
        const val CLEAR_WHEN_EMPTY_TAG = "ClearWhenEmpty"
        const val UPGRADE_TAG = "Upgrades"
        const val MAX_UPGRADES_TAG = "MaxUpgrades"

        const val STORED_BLOCK_ENTITY_TAG = "StoredBlockEntity"

        val TYPE: BlockEntityType<ItemBarrelBlockEntity> =
            BlockEntityType.Builder.create(Supplier { ItemBarrelBlockEntity() }, ItemBarrelBlock).build(null)

        fun fromStack(stack: ItemStack): ItemBarrelBlockEntity {
            val barrel = ItemBarrelBlockEntity()
            barrel.fromClientTag(stack.getOrCreateSubTag(STORED_BLOCK_ENTITY_TAG))
            return barrel
        }
    }

    val invAttribute = FixedMassItemInv(storage, false)
    private val invWrapper = UnrestrictedInventoryFixedWrapper(invAttribute)

    init {
        invAttribute.addListener({ _, _, _, _ -> markDirty() }, { })
    }

    override fun markDirty() {
        if (clearWhenEmpty && storage.isEmpty) {
            storage.contents = ItemStackQuantizer.NONE
        }

        handleUpgradeChanges()
        super.markDirty()

        world?.apply {
            updateListeners(pos, getBlockState(pos), getBlockState(pos), 3)
        }
    }

    // TODO: Delegate to observable?
    private fun handleUpgradeChanges() {
        invAttribute.voidExtraItems = appliedUpgrades.filterIsInstance<TrashUpgrade>().any()

        // TODO: Lame
        (storage as MassItemStackStorage).maxStacks = appliedUpgrades.filterIsInstance<ItemBarrelUpgrade>()
            .fold(DEFAULT_CAPACITY_STACKS) { acc, upgrade -> upgrade.upgradeMaxStacks(acc) }
    }

    override fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        val removedItems = when (amount) {
            BarrelTransactionAmount.ONE -> storage.contents.withAmount(1)
            BarrelTransactionAmount.MANY -> storage.contents.reference.oneStackToQuantizer()
            BarrelTransactionAmount.ALL -> TODO()
        }

        storage.removeAtMost(removedItems).toObjects().forEach { it.giveTo(player) }
        markDirty()
    }

    override fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        when (amount) {
            BarrelTransactionAmount.ONE -> TODO()

            BarrelTransactionAmount.MANY -> {
                if (!player.mainHandStack.isEmpty) player.setStackInHand(
                    Hand.MAIN_HAND,
                    storage.addAtMost(player.mainHandStack.toQuantizer()).firstStack()
                )
            }

            BarrelTransactionAmount.ALL -> {
                player.inventory.main.replaceAll { storage.addAtMost(it.toQuantizer()).firstStack() }
            }
        }

        player.inventory.markDirty()
        markDirty()
    }

    override fun changeModes() {
        clearWhenEmpty = !clearWhenEmpty
        markDirty()
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        requireNotNull(tag)
        return toClientTag(super.toTag(tag))
    }

    override fun fromTag(tag: CompoundTag?) {
        requireNotNull(tag)

        super.fromTag(tag)
        fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag = requireNotNull(tag).apply {
        putString(STORED_ITEM_TAG, Registry.ITEM.getId(storage.contents.reference.item).toString())
        putLong(AMOUNT_STORED_TAG, storage.contents.amount)
        putBoolean(CLEAR_WHEN_EMPTY_TAG, clearWhenEmpty)
        put(UPGRADE_TAG, appliedUpgrades.mapNotNull { u -> UpgradeRegistry.writeUpgrade(u) }.toCollection(ListTag()))
    }

    override fun fromClientTag(tag: CompoundTag?) = requireNotNull(tag).run {
        // Upgrades
        maxUpgrades = if (containsKey(MAX_UPGRADES_TAG)) {
            getInt(MAX_UPGRADES_TAG)
        } else {
            DEFAULT_MAX_UPGRADES
        }

        appliedUpgrades = getList(UPGRADE_TAG, NbtType.COMPOUND)
            .take(maxUpgrades)
            .mapNotNull { t -> UpgradeRegistry.readUpgrade(t as? CompoundTag ?: return@mapNotNull null) }

        // State
        handleUpgradeChanges()
        clearWhenEmpty = getBoolean(CLEAR_WHEN_EMPTY_TAG)

        // Contents
        val itemName = getString(STORED_ITEM_TAG)
        val itemId = Identifier.tryParse(itemName) ?: Registry.ITEM.defaultId

        if (itemName.isNullOrBlank() || itemId == Registry.ITEM.defaultId || !Registry.ITEM.containsId(itemId)) {
            storage.contents = ItemStackQuantizer.NONE
        } else {
            val itemAmount = min(max(0L, getLong(AMOUNT_STORED_TAG)), storage.capacity)
            storage.contents = ItemStackQuantizer(ItemStack(Registry.ITEM[itemId], 1), itemAmount)
        }
    }

    // Delegation of Inventory to invAttribute. As far as I know we can't use Kotlin's implementation by delegation
    // because the implementation can change.

    override fun getInvStack(slot: Int): ItemStack = invWrapper.getInvStack(slot)

    override fun clear() = invWrapper.clear()

    override fun setInvStack(slot: Int, stack: ItemStack?) = invWrapper.setInvStack(slot, stack)

    override fun removeInvStack(slot: Int): ItemStack = invWrapper.removeInvStack(slot)

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean = invWrapper.canPlayerUseInv(player)

    override fun getInvSize(): Int = invWrapper.invSize

    override fun takeInvStack(slot: Int, amount: Int): ItemStack = invWrapper.takeInvStack(slot, amount)

    override fun isInvEmpty(): Boolean = invWrapper.isInvEmpty

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean = invWrapper.isValidInvStack(slot, stack)
}

