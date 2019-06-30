package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.upgrade.Upgrade
import me.branchpanic.mods.stockpile.api.upgrade.UpgradeContainer
import me.branchpanic.mods.stockpile.api.upgrade.barrel.ItemBarrelUpgrade
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.content.upgrade.TrashUpgrade
import me.branchpanic.mods.stockpile.giveTo
import me.branchpanic.mods.stockpile.impl.attribute.FixedMassItemInv
import me.branchpanic.mods.stockpile.impl.attribute.UnrestrictedInventoryFixedWrapper
import me.branchpanic.mods.stockpile.impl.storage.MassItemStorage
import me.branchpanic.mods.stockpile.impl.upgrade.UpgradeRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import java.text.NumberFormat
import java.util.*
import java.util.function.Supplier
import kotlin.math.min

class ItemBarrelBlockEntity(
    private var storage: MassItemStorage = MassItemStorage(
        DEFAULT_CAPACITY_STACKS
    ),
    override var appliedUpgrades: List<ItemBarrelUpgrade> = emptyList()
) :
    BlockEntity(TYPE),
    BlockEntityClientSerializable,
    UpgradeContainer,
    Inventory {
    val invAttribute: FixedMassItemInv = FixedMassItemInv(storage)
    private val invWrapper = UnrestrictedInventoryFixedWrapper(invAttribute)

    init {
        invAttribute.addListener({ _, _, _, _ -> markDirty() }, {})
    }

    override fun getInvStack(slot: Int): ItemStack = invWrapper.getInvStack(slot)

    override fun clear() = invWrapper.clear()

    override fun setInvStack(p0: Int, p1: ItemStack?) = invWrapper.setInvStack(p0, p1)

    override fun removeInvStack(p0: Int): ItemStack = invWrapper.removeInvStack(p0)

    override fun canPlayerUseInv(p0: PlayerEntity?): Boolean = invWrapper.canPlayerUseInv(p0)

    override fun getInvSize(): Int = invWrapper.invSize

    override fun takeInvStack(p0: Int, p1: Int): ItemStack = invWrapper.takeInvStack(p0, p1)

    override fun isInvEmpty(): Boolean = invWrapper.isInvEmpty

    override fun isValidInvStack(int_1: Int, itemStack_1: ItemStack?): Boolean =
        invWrapper.isValidInvStack(int_1, itemStack_1)

    override val maxUpgrades: Int = MAX_UPGRADES

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

        val TYPE: BlockEntityType<ItemBarrelBlockEntity> =
            BlockEntityType.Builder.create(Supplier { ItemBarrelBlockEntity() }, ItemBarrelBlock).build(null)

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
                player.addChatMessage(TranslatableComponent("ui.stockpile.barrel.just_locked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.1f, 0.9f)
            } else {
                storage.clearInstanceWhenEmpty()
                player.addChatMessage(TranslatableComponent("ui.stockpile.barrel.just_unlocked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.1f, 0.9f)
            }

            markDirty()
            return
        }

        recentUsers = if (player.uuid in recentUsers && storage.instanceIsSet) {
            player.inventory.main.replaceAll { s -> storage.offer(s) }
            player.inventory.offHand.replaceAll { s -> storage.offer(s) }
            player.inventory.markDirty()

            recentUsers - player.uuid
        } else {
            val activeStack = player.getStackInHand(Hand.MAIN_HAND)
            val resultingStack = storage.offer(activeStack)

            player.setStackInHand(Hand.MAIN_HAND, resultingStack)
            player.inventory.markDirty()

            recentUsers + (player.uuid to System.currentTimeMillis())
        }

        markDirty()
        showContents(player)
    }

    private fun showContents(player: PlayerEntity) {
        player.addChatMessage(getContentDescription(), true)
    }

    override fun markDirty() {
        super.markDirty()
        invWrapper.markDirty()

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
            upgradeTags.mapNotNull { t -> (t as? CompoundTag)?.let { c -> UpgradeRegistry.readUpgrade(c) as? ItemBarrelUpgrade } }

        val capacityStacks = appliedUpgrades.fold(DEFAULT_CAPACITY_STACKS) { i, u -> u.upgradeMaxStacks(i) }

        storage = MassItemStorage(
            capacityStacks,
            min(amountStored, capacityStacks.toLong() * storedItem.maxAmount),
            storedItem,
            clearWhenEmpty
        )

        invAttribute.storage = storage
        invAttribute.voidExtraItems = appliedUpgrades.filterIsInstance<TrashUpgrade>().any()
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag = toTag(tag ?: CompoundTag())

    override fun fromClientTag(tag: CompoundTag?) = fromTag(tag ?: CompoundTag())

    override fun isUpgradeTypeAllowed(u: Upgrade): Boolean = u is ItemBarrelUpgrade

    override fun pushUpgrade(u: Upgrade) {
        if (u !is ItemBarrelUpgrade) {
            Stockpile.LOGGER.warn("attempted to apply an invalid upgrade (type ${u.id}) to an item barrel")
            Stockpile.LOGGER.debug("this is a bug! was isUpgradeTypeAllowed checked?")
            return
        }

        appliedUpgrades += u

        fromTagWithoutWorldInfo(toTagWithoutWorldInfo(CompoundTag()))
        markDirty()
    }

    override fun popUpgrade(): Upgrade {
        val result = appliedUpgrades.last()
        appliedUpgrades = appliedUpgrades.dropLast(1)

        fromTagWithoutWorldInfo(toTagWithoutWorldInfo(CompoundTag())) // TODO: what?
        markDirty()

        return result
    }

    fun toStack(stack: ItemStack) {
        toTagWithoutWorldInfo(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
    }

    fun fromStack(stack: ItemStack) {
        fromTagWithoutWorldInfo(stack.getOrCreateSubCompoundTag(STORED_BLOCK_ENTITY_TAG))
    }

    fun getContentDescription(): Component {
        val f = NumberFormat.getInstance()

        return if (storage.isEmpty) {
            TranslatableComponent("ui.stockpile.barrel.contents_empty", f.format(storage.maxStacks))
        } else {
            TranslatableComponent(
                "ui.stockpile.barrel.contents",
                f.format(storage.amountStored),
                f.format(storage.capacity),
                storage.currentInstance.displayName.formattedText,
                f.format(storage.amountStored / storage.currentInstance.maxAmount),
                f.format(storage.maxStacks)
            )
        }
    }
}
