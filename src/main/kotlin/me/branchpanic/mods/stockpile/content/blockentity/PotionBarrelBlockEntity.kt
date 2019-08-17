package me.branchpanic.mods.stockpile.content.blockentity

import me.branchpanic.mods.stockpile.api.AbstractBarrelBlockEntity
import me.branchpanic.mods.stockpile.api.BarrelTransactionAmount
import me.branchpanic.mods.stockpile.content.block.BarrelBlock
import me.branchpanic.mods.stockpile.extension.giveTo
import me.branchpanic.mods.stockpile.extension.withCount
import me.branchpanic.mods.stockpile.impl.storage.MassPotionStorage
import me.branchpanic.mods.stockpile.impl.storage.PotionQuantizer
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.GlassBottleItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.PotionItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

class PotionBarrelBlockEntity : AbstractBarrelBlockEntity<Potion>(
    clearWhenEmpty = false,
    storage = MassPotionStorage(capacity = 32L, contents = PotionQuantizer.NONE),
    doubleClickThresholdMs = 1000,
    type = TYPE
), BlockEntityClientSerializable {
    companion object {
        const val STORED_POTION_TAG = "StoredPotion"

        val TYPE: BlockEntityType<PotionBarrelBlockEntity> =
            BlockEntityType.Builder.create(Supplier { PotionBarrelBlockEntity() }, BarrelBlock.POTION).build(null)
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

    override fun toClientTag(tag: CompoundTag): CompoundTag = tag.apply {
        putString(STORED_POTION_TAG, Registry.POTION.getId(storage.contents.reference).toString())
        putLong(ItemBarrelBlockEntity.AMOUNT_STORED_TAG, storage.contents.amount)
        putBoolean(ItemBarrelBlockEntity.CLEAR_WHEN_EMPTY_TAG, clearWhenEmpty)
    }

    override fun fromClientTag(tag: CompoundTag) = tag.run {
        // State
        clearWhenEmpty = getBoolean(ItemBarrelBlockEntity.CLEAR_WHEN_EMPTY_TAG)

        // Contents
        val potion = Registry.POTION[Identifier(getString(STORED_POTION_TAG))]
        if (potion == Potions.EMPTY) {
            storage.contents = PotionQuantizer.NONE
        } else {
            val itemAmount = min(max(0L, getLong(ItemBarrelBlockEntity.AMOUNT_STORED_TAG)), storage.capacity)
            storage.contents = PotionQuantizer(potion, itemAmount)
        }
    }
}
