package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.inventory.MassItemInventory
import me.branchpanic.mods.stockpile.api.storage.MassItemStorage
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeApplier
import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeRegistry
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TranslatableTextComponent
import java.text.NumberFormat
import java.util.*
import kotlin.math.min

class ItemBarrelBlockEntity(
    private var storage: MassItemStorage = MassItemStorage(DEFAULT_CAPACITY_STACKS),
    override var appliedUpgrades: List<ItemBarrelUpgrade> = emptyList(),
    private val invWrapper: MassItemInventory = MassItemInventory(storage)
) :
    BlockEntity(TYPE),
    BlockEntityClientSerializable,
    UpgradeApplier,
    SidedInventory by invWrapper {

    override fun canApplyUpgrade(u: Upgrade): Boolean = appliedUpgrades.size < MAX_UPGRADES && u is ItemBarrelUpgrade

    override fun applyUpgrade(u: Upgrade) {
        if (u !is ItemBarrelUpgrade) {
            Stockpile.LOGGER.warn("attempted to apply an invalid upgrade (type ${u.id}) to an item barrel")
            Stockpile.LOGGER.debug("this is a bug! was canApplyUpgrade checked?")
            return
        }

        appliedUpgrades += u

        fromTagWithoutWorldInfo(toTagWithoutWorldInfo(CompoundTag()))
        markDirty()
    }

    constructor(tag: CompoundTag) : this() {
        fromTag(tag)
    }

    companion object {
        const val DEFAULT_CAPACITY_STACKS = 32
        const val MAX_UPGRADES = 6

        const val STORED_ITEM_TAG = "StoredItem"
        const val AMOUNT_STORED_TAG = "AmountStored"
        const val CLEAR_WHEN_EMPTY_TAG = "ClearWhenEmpty"
        const val UPGRADE_TAG = "Upgrades"

        const val RIGHT_CLICK_PERIOD_MS = 500

        val TYPE: BlockEntityType<ItemBarrelBlockEntity> = BlockEntityType({ ItemBarrelBlockEntity() }, null)

        const val STORED_BLOCK_ENTITY_TAG = "StoredBlockEntity"

        fun loadFromStack(stack: ItemStack): ItemBarrelBlockEntity =
            ItemBarrelBlockEntity(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
    }

    val backingStorage get() = storage

    private var recentUsers: Map<UUID, Long> = mapOf()

    fun onPunched(player: PlayerEntity) {
        if (world?.isClient != false || storage.isEmpty) {
            return
        }

        if (player.isSneaking) {
            storage.take(storage.currentInstance.maxAmount.toLong())[0].giveTo(player)
        } else {
            storage.take(1)[0].giveTo(player)
        }

        markDirty()
        showContents(player)
    }

    fun onActivated(player: PlayerEntity) {
        if (world?.isClient != false) {
            return
        }

        recentUsers = recentUsers.filterValues { t -> System.currentTimeMillis() - t <= RIGHT_CLICK_PERIOD_MS }

        if (player.isSneaking) {
            if (storage.clearWhenEmpty) {
                storage.retainInstanceWhenEmpty()
                player.addChatMessage(TranslatableTextComponent("ui.stockpile.barrel.just_locked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.1f, 0.9f)
            } else {
                storage.clearInstanceWhenEmpty()
                player.addChatMessage(TranslatableTextComponent("ui.stockpile.barrel.just_unlocked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.1f, 0.9f)
            }

            markDirty()
            return
        }

        recentUsers = if (player.uuid in recentUsers && !storage.isEmpty) {
            player.inventory.main.replaceAll { s -> storage.offer(s) ?: ItemStack.EMPTY }
            player.inventory.offHand.replaceAll { s -> storage.offer(s) ?: ItemStack.EMPTY }
            player.inventory.markDirty()

            recentUsers - player.uuid
        } else {
            val activeStack = player.getStackInHand(player.activeHand)
            val resultingStack = storage.offer(activeStack) ?: ItemStack.EMPTY

            player.setStackInHand(player.activeHand, resultingStack)
            player.inventory.markDirty()

            recentUsers + (player.uuid to System.currentTimeMillis())
        }

        markDirty()
        showContents(player)
    }

    private fun showContents(player: PlayerEntity) {
        val message = if (storage.isEmpty) {
            TranslatableTextComponent("ui.stockpile.empty")
        } else {
            val f = NumberFormat.getInstance()

            TranslatableTextComponent(
                "ui.stockpile.barrel.contents_world",
                f.format(storage.amountStored),
                f.format(storage.capacity),
                storage.currentInstance.displayName.formattedText,
                f.format(storage.amountStored / storage.currentInstance.maxAmount),
                f.format(storage.capacity / storage.currentInstance.maxAmount)
            )
        }

        player.addChatMessage(message, true)
    }

    override fun markDirty() {
        super.markDirty()

        world?.apply {
            updateListeners(pos, getBlockState(pos), getBlockState(pos), 3)
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        return toTagWithoutWorldInfo(super.toTag(tag))
    }

    private fun toTagWithoutWorldInfo(tag: CompoundTag): CompoundTag {
        tag.put(STORED_ITEM_TAG, storage.currentInstance.toTag(CompoundTag()))
        tag.putLong(AMOUNT_STORED_TAG, storage.amountStored)
        tag.putBoolean(CLEAR_WHEN_EMPTY_TAG, storage.clearWhenEmpty)

        val upgradeTags = ListTag()
        appliedUpgrades.forEach { u -> upgradeTags.add(UpgradeRegistry.writeUpgrade(u)) }

        tag.put(UPGRADE_TAG, upgradeTags)

        return tag
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)

        if (tag == null) {
            Stockpile.LOGGER.warn("an item barrel at $pos is missing data and will be reset to defaults")
            return
        }

        fromTagWithoutWorldInfo(tag)
    }

    private fun fromTagWithoutWorldInfo(tag: CompoundTag) {
        val storedItem = ItemStack.fromTag(tag.getCompound(STORED_ITEM_TAG))
        val amountStored = tag.getLong(AMOUNT_STORED_TAG)
        val clearWhenEmpty = tag.getBoolean(CLEAR_WHEN_EMPTY_TAG)

        val upgradeTags = tag.getList(UPGRADE_TAG, NbtType.COMPOUND).take(MAX_UPGRADES)

        appliedUpgrades =
            upgradeTags.mapNotNull { t -> (t as? CompoundTag)?.let { c -> UpgradeRegistry.readUpgrade(c) as ItemBarrelUpgrade } }

        val capacityStacks = appliedUpgrades.fold(DEFAULT_CAPACITY_STACKS) { i, u -> u.upgradeMaxStacks(i) }

        storage = MassItemStorage(
            capacityStacks,
            min(amountStored, capacityStacks.toLong() * storedItem.maxAmount),
            storedItem,
            clearWhenEmpty
        )

        invWrapper.storage = storage
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag = toTag(tag ?: CompoundTag())

    override fun fromClientTag(tag: CompoundTag?) = fromTag(tag ?: CompoundTag())

    fun toStack(stack: ItemStack) {
        toTagWithoutWorldInfo(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
    }

    fun fromStack(stack: ItemStack) {
        fromTagWithoutWorldInfo(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
    }
}

private fun ItemStack.giveTo(player: PlayerEntity) {
    player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.1f, 0.7f)
    player.inventory.insertStack(this)

    if (isEmpty) {
        return
    }

    player.world.spawnEntity(ItemEntity(player.world, player.x, player.y, player.z, this))
}
