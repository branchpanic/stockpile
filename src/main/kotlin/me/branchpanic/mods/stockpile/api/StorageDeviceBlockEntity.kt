@file:Suppress("DEPRECATION", "UnstableApiUsage")

package me.branchpanic.mods.stockpile.api

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.*

// TODO: Config
const val doubleTapTimeMs: Long = 100

/**
 * StorageDeviceBlockEntity provides the interaction logic for implementing a "Barrel"-type block (generalized here
 * as "storage device"). It implements the following interactions:
 *
 *  - 1x primary (i.e. left mouse button)    = extract one
 *  - 1x primary while sneaking              = extract many
 *  - 1x secondary (i.e. right mouse button) = insert many
 *  - 2x secondary                           = insert all
 *  - shift + secondary                      = toggle lock state
 *
 * NB: the block that owns this BlockEntity needs to call onPrimaryInteraction and onSecondaryInteraction at the
 * appropriate times.
 */
abstract class StorageDeviceBlockEntity(
    blockEntityType: BlockEntityType<*>?,
    blockPos: BlockPos?,
    blockState: BlockState?
) : BlockEntity(blockEntityType, blockPos, blockState) {
    private var recentUsers: MutableMap<UUID, Long> = mutableMapOf()

    fun onPrimaryInteraction(player: PlayerEntity) {
        if (!hasWorld()) return
        if (world!!.isClient()) return

        val amount =
            if (player.isSneaking) FuzzyTransactionAmount.MANY
            else FuzzyTransactionAmount.ONE

        giveToPlayer(player, amount)
    }

    fun onSecondaryInteraction(player: PlayerEntity) {
        if (!hasWorld()) return
        if (world!!.isClient()) return

        if (player.isSneaking) {
            locked = !locked
            return
        }

        val nowMs = System.currentTimeMillis()
        recentUsers.values.retainAll { lastUsed -> (nowMs - lastUsed) > doubleTapTimeMs }

        val playerUuid = player.uuid
        val isDoubleTap = playerUuid in recentUsers
        val amount: FuzzyTransactionAmount

        if (isDoubleTap) {
            recentUsers.remove(playerUuid)
            amount = FuzzyTransactionAmount.ALL
        } else {
            recentUsers[playerUuid] = nowMs
            amount = FuzzyTransactionAmount.MANY
        }

        takeFromPlayer(player, amount)
    }

    abstract fun giveToPlayer(player: PlayerEntity, amount: FuzzyTransactionAmount): Long
    abstract fun takeFromPlayer(player: PlayerEntity, amount: FuzzyTransactionAmount): Long
    abstract var locked: Boolean
}
