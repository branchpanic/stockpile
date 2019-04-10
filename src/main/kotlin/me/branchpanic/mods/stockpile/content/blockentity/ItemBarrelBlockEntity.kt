package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.api.inventory.MassItemInventory
import me.branchpanic.mods.stockpile.api.storage.MassItemStorage
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

class ItemBarrelBlockEntity(
    private var storage: MassItemStorage = MassItemStorage(DEFAULT_CAPACITY_STACKS),
    private val invWrapper: MassItemInventory = MassItemInventory(storage)
) :
    BlockEntity(TYPE),
    SidedInventory by invWrapper {

    companion object {
        const val DEFAULT_CAPACITY_STACKS = 32
        const val STORED_ITEM_TAG = "StoredItem"
        const val AMOUNT_STORED_TAG = "AmountStored"
        const val CLEAR_WHEN_EMPTY_TAG = "ClearWhenEmpty"

        val TYPE: BlockEntityType<ItemBarrelBlockEntity> = BlockEntityType({ ItemBarrelBlockEntity() }, null)
    }

    fun onPunched(player: PlayerEntity) {
    }

    fun onActivated(player: PlayerEntity) {
    }

    override fun markDirty() {
        invWrapper.markDirty()
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        val baseTag = super.toTag(tag)

        baseTag.put(STORED_ITEM_TAG, storage.currentInstance.toTag(CompoundTag()))
        baseTag.putLong(AMOUNT_STORED_TAG, storage.amountStored)
        baseTag.putBoolean(CLEAR_WHEN_EMPTY_TAG, storage.clearWhenEmpty)

        return baseTag
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)

        if (tag == null) {
            Stockpile.LOGGER.warn("item barrel at $pos is missing data and will be reset to defaults")
            return
        }

        val storedItem = ItemStack.fromTag(tag.getCompound(STORED_ITEM_TAG))
        val amountStored = tag.getLong(AMOUNT_STORED_TAG)
        val clearWhenEmpty = tag.getBoolean(CLEAR_WHEN_EMPTY_TAG)

        storage = MassItemStorage(DEFAULT_CAPACITY_STACKS, amountStored, storedItem, clearWhenEmpty)
    }
}