package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.BarrelTransactionAmount
import me.branchpanic.mods.stockpile.content.block.ItemBarrelBlock
import me.branchpanic.mods.stockpile.extension.giveTo
import me.branchpanic.mods.stockpile.extension.withCount
import me.branchpanic.mods.stockpile.impl.storage.MassPotionStorage
import me.branchpanic.mods.stockpile.impl.storage.PotionQuantizer
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.GlassBottleItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.PotionItem
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionUtil
import net.minecraft.util.Hand
import java.util.function.Supplier
import kotlin.math.min

class PotionBarrelBlockEntity(
    var clearWhenEmpty: Boolean = false
) : AbstractBarrelBlockEntity<Potion>(
    storage = MassPotionStorage(capacity = 32L, contents = PotionQuantizer.NONE),
    doubleClickThresholdMs = 1000,
    type = TYPE
) {
    companion object {
        val TYPE: BlockEntityType<PotionBarrelBlockEntity> =
            BlockEntityType.Builder.create(Supplier { PotionBarrelBlockEntity() }, ItemBarrelBlock).build(null)
    }

    override fun giveToPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        if (storage.isEmpty) return
        if (player.mainHandStack.item !is GlassBottleItem) return

        when (amount) {
            BarrelTransactionAmount.ONE -> {
                val leftoverBottles = if (player.mainHandStack.count <= 1) {
                    ItemStack.EMPTY
                } else {
                    player.mainHandStack.withCount(player.mainHandStack.count - 1)
                }

                val extractedPotionStack = ItemStack(Items.POTION, 1)
                PotionUtil.setPotion(extractedPotionStack, storage.contents.reference)
                storage.removeAtMost(1)

                if (!leftoverBottles.isEmpty) {
                    // If there are leftover empty bottles, leave them in the main hand to make repeated extraction
                    // easier.
                    player.setStackInHand(Hand.MAIN_HAND, leftoverBottles)
                    extractedPotionStack.giveTo(player)
                } else {
                    player.setStackInHand(Hand.MAIN_HAND, extractedPotionStack)
                }
            }
            BarrelTransactionAmount.MANY -> {
                // If someone extracts 64 bottles' worth with a full inventory, they'll all be thrown on the ground.
                // That's just lag waiting to happen, so we cap it based on free inventory slots.

                val bottles = min(player.mainHandStack.count, player.inventory.main.count { it.isEmpty })
                val potion = storage.contents.reference
                val extractedBottles = storage.removeAtMost(bottles.toLong())
                val leftoverBottles = player.mainHandStack.count - extractedBottles

                player.setStackInHand(Hand.MAIN_HAND, player.mainHandStack.withCount(leftoverBottles.toInt()))

                generateSequence {
                    val extractedPotionStack = ItemStack(Items.POTION, 1)
                    PotionUtil.setPotion(extractedPotionStack, potion)
                }.take(extractedBottles.toInt()).forEach { it.giveTo(player) }
            }
            BarrelTransactionAmount.ALL -> TODO()
        }

        markDirty()
    }

    override fun markDirty() {
        if (clearWhenEmpty && storage.isEmpty) {
            storage.contents = PotionQuantizer.NONE
        }

        super.markDirty()

        world?.apply {
            updateListeners(pos, getBlockState(pos), getBlockState(pos), 3)
        }
    }

    override fun takeFromPlayer(player: PlayerEntity, amount: BarrelTransactionAmount) {
        when (amount) {
            BarrelTransactionAmount.ONE -> TODO()

            BarrelTransactionAmount.MANY -> {
                if (player.mainHandStack.item is PotionItem) {
                    val potion = PotionUtil.getPotion(player.mainHandStack)
                    val quantizer = PotionQuantizer(potion, 1)

                    if (storage.contents.canMergeWith(quantizer)) {
                        val remainder = storage.addAtMost(quantizer)
                        if (remainder.amount < 1) {
                            player.setStackInHand(Hand.MAIN_HAND, ItemStack(Items.GLASS_BOTTLE))
                        }
                    }
                }
            }

            BarrelTransactionAmount.ALL -> {
                player.inventory.main.replaceAll r@{
                    if (it.item is PotionItem) {
                        val potion = PotionUtil.getPotion(it)
                        val quantizer = PotionQuantizer(potion, 1)

                        if (storage.contents.canMergeWith(quantizer)) {
                            val remainder = storage.addAtMost(quantizer)
                            if (remainder.amount < 1) {
                                return@r ItemStack(Items.GLASS_BOTTLE)
                            }
                        }
                    }

                    return@r it
                }
            }
        }

        player.inventory.markDirty()
        markDirty()
    }

    override fun changeModes(player: PlayerEntity) {
        clearWhenEmpty = !clearWhenEmpty
    }
}
