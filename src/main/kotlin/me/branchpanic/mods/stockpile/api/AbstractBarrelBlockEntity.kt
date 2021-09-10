package me.branchpanic.mods.stockpile.api

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * A BarrelTransactionAmount is a fuzzy quantity for content to be inserted/extracted from a barrel. For example,
 * with an item barrel:
 *
 *  - ONE = one item
 *  - MANY = one stack
 *  - ALL = one inventory's worth
 *
 * However, these values are not strict: use your best judgement when consuming these.
 */
enum class BarrelTransactionAmount {
    ONE,
    MANY,
    ALL
}

/**
 * An AbstractBarrelBlockEntity implements interaction logic for a barrel-like block entity.
 */
abstract class AbstractBarrelBlockEntity<T>(
    open val storage: MutableMassStorage<T>,
    open var clearWhenEmpty: Boolean,
    private val doubleClickThresholdMs: Long,
    type: BlockEntityType<*>, blockPos: BlockPos, blockState: BlockState
) : BlockEntity(type, blockPos, blockState), BlockEntityClientSerializable {
    private var userCache: Map<UUID, Long> = emptyMap()

    fun onRightClicked(player: PlayerEntity) {
        if (world?.isClient != false) {
            return
        }

        val now = System.currentTimeMillis()
        userCache = userCache.filterValues { activatedTime -> now - activatedTime < doubleClickThresholdMs }

        if (player.isSneaking) {
            changeModes(player)
            return
        }

        // Lifting the assignment to userCache out of this statement would result in a pretty weird-looking expression
        // with side effects.
        @Suppress("LiftReturnOrAssignment")
        if (player.uuid in userCache) {
            takeFromPlayer(player, BarrelTransactionAmount.ALL)
            userCache = userCache - player.uuid
        } else {
            takeFromPlayer(player, BarrelTransactionAmount.MANY)
            userCache = userCache + (player.uuid to now)
        }

        showStatusToPlayer(player)
    }

    fun onLeftClicked(player: PlayerEntity) {
        if (world?.isClient != false) {
            return
        }

        giveToPlayer(player, if (player.isSneaking) BarrelTransactionAmount.MANY else BarrelTransactionAmount.ONE)
        showStatusToPlayer(player)
    }

    private fun showStatusToPlayer(player: PlayerEntity) {
        player.sendMessage(storage.describeContents(), true)
    }

    private fun changeModes(player: PlayerEntity) {
        clearWhenEmpty = !clearWhenEmpty

        if (clearWhenEmpty) {
            player.sendMessage(TranslatableText("ui.stockpile.barrel.just_unlocked"), true)
            player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5f, 1.1f)
        } else {
            player.sendMessage(TranslatableText("ui.stockpile.barrel.just_locked"), true)
            player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5f, 0.9f)
        }

        markDirty()
    }

    abstract fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)
    abstract fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        requireNotNull(tag)
        return toClientTag(super.writeNbt(tag))
    }

    override fun readNbt(tag: NbtCompound?) {
        requireNotNull(tag)

//        if (state != null) {
            super.readNbt(tag)
//        }

        fromClientTag(tag)
    }
}
