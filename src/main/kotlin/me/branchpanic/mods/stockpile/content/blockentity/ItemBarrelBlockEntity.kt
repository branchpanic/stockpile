package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.BarrelTransactionAmount
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.impl.attribute.FixedMassItemInv
import me.branchpanic.mods.stockpile.impl.attribute.UnrestrictedInventoryFixedWrapper
import me.branchpanic.mods.stockpile.impl.storage.ItemStackQuantizer
import me.branchpanic.mods.stockpile.impl.storage.MassItemStackStorage
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

class ItemBarrelBlockEntity(
    private var clearWhenEmpty: Boolean = true
) : AbstractBarrelBlockEntity<ItemStack>(
    storage = MassItemStackStorage(ItemStackQuantizer.NONE, DEFAULT_CAPACITY_STACKS),
    doubleClickThresholdMs = 5000,
    type = TYPE
), BlockEntityClientSerializable {
    companion object {
        const val DEFAULT_CAPACITY_STACKS = 32
        const val MAX_UPGRADES = 6

        const val STORED_ITEM_TAG = "StoredItem"
        const val AMOUNT_STORED_TAG = "AmountStored"
        const val CLEAR_WHEN_EMPTY_TAG = "ClearWhenEmpty"
        const val UPGRADE_TAG = "Upgrades"

        val TYPE: BlockEntityType<ItemBarrelBlockEntity> =
            BlockEntityType.Builder.create(Supplier { ItemBarrelBlockEntity() }, ItemBarrelBlock).build(null)

        const val STORED_BLOCK_ENTITY_TAG = "StoredBlockEntity"

        fun loadFromStack(stack: ItemStack): ItemBarrelBlockEntity = ItemBarrelBlockEntity()
    }

    // TODO(perf): Cache and re-create when needed by observing this.storage
    val invAttribute
        get() = UnrestrictedInventoryFixedWrapper(FixedMassItemInv(storage, false))

    override fun markDirty() {
        if (clearWhenEmpty && storage.isEmpty) {
            storage.contents = ItemStackQuantizer.NONE
        }

        super.markDirty()
    }

    override fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {

    }

    override fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {

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
    }

    override fun fromClientTag(tag: CompoundTag?) = requireNotNull(tag).run {
        clearWhenEmpty = tag.getBoolean(CLEAR_WHEN_EMPTY_TAG)

        val itemName = tag.getString(STORED_ITEM_TAG)
        val itemId = Identifier.tryParse(itemName) ?: Registry.ITEM.defaultId

        if (itemName.isNullOrBlank() || itemId == Registry.ITEM.defaultId || !Registry.ITEM.containsId(itemId)) {
            storage.contents = ItemStackQuantizer.NONE
        } else {
            val itemAmount = max(min(0L, tag.getLong(AMOUNT_STORED_TAG)), storage.capacity)
            storage.contents = ItemStackQuantizer(ItemStack(Registry.ITEM[itemId], 1), itemAmount)
        }
    }
}
