package me.branchpanic.mods.stockpile.api

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
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
    private val doubleClickThresholdMs: Long,
    type: BlockEntityType<*>
) : BlockEntity(type) {
    private var userCache: Map<UUID, Long> = emptyMap()

    fun onRightClicked(player: PlayerEntity) {
        if (world?.isClient != false) {
            return
        }

        val now = System.currentTimeMillis()
        userCache = userCache.filterValues { activatedTime -> now - activatedTime < doubleClickThresholdMs }

        if (player.isSneaking) {
            changeModes()
            return
        }

        if (player.uuid in userCache) {
            takeFromPlayer(player, BarrelTransactionAmount.ALL)
            userCache = userCache - player.uuid
            return
        }

        takeFromPlayer(player, BarrelTransactionAmount.MANY)
        userCache = userCache + (player.uuid to now)

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

    abstract fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)
    abstract fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount)
    abstract fun changeModes()
}
