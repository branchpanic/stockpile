package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions

class PotionQuantizer(override val reference: Potion, override val amount: Long) : Quantizer<Potion> {
    companion object {
        val NONE = PotionQuantizer(reference = Potions.EMPTY, amount = 0L)
    }

    override fun canMergeWith(other: Quantizer<Potion>): Boolean {
        return reference == other.reference || reference == Potions.EMPTY || other.reference == Potions.EMPTY
    }

    override fun withAmount(amount: Long): Quantizer<Potion> {
        return PotionQuantizer(reference, amount)
    }

    override fun toObjects(): List<Potion> {
        return generateSequence { reference }.take(amount.toInt()).toList()
    }

    override fun plus(other: Quantizer<Potion>): Quantizer<Potion> {
        val untypedResult = super.plus(other)

        return if (reference == Potions.EMPTY) {
            PotionQuantizer(other.reference, untypedResult.amount)
        } else {
            untypedResult
        }
    }
}
