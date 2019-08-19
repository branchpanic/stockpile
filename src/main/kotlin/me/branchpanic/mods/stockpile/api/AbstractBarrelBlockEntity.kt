package me.branchpanic.mods.stockpile.api

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TranslatableText
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
    type: BlockEntityType<*>
) : BlockEntity(type), BlockEntityClientSerializable {
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
        player.addChatMessage(storage.describeContents(), true)
    }

    private fun changeModes(player: PlayerEntity) {
        clearWhenEmpty = !clearWhenEmpty

        if (clearWhenEmpty) {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel.just_unlocked"), true)
            player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5f, 1.1f)
        } else {
            player.addChatMessage(TranslatableText("ui.stockpile.barrel.just_locked"), true)
            player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5f, 0.9f)
        }

        markDirty()
    }

    abstract fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)
    abstract fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)

    override fun toTag(tag: CompoundTag?): CompoundTag {
        requireNotNull(tag)
        return toClientTag(super.toTag(tag))
    }

    override fun fromTag(tag: CompoundTag?) {
        requireNotNull(tag)

        super.fromTag(tag)
        fromClientTag(tag)
    }
}
