package me.branchpanic.mods.stockpile.api.storage

import net.minecraft.text.Text

/**
 * A MassStorage tracks a large quantity (one Long's worth, but this will most likely never be reached) of a given
 * instance.
 */
interface MassStorage<T> {
    /**
     * The contents of this MassStorage.
     */
    val contents: Quantizer<T>

    /**
     * The upper bound on this MassStorage's amount.
     */
    val capacity: Long

    /**
     * Determines whether or not this MassStorage is empty.
     */
    val isEmpty: Boolean
        get() = contents.amount <= 0L

    /**
     * Determines whether or not this MassStorage is full.
     */
    val isFull: Boolean
        get() = contents.amount >= capacity

    /**
     * The amount of free space in this MassStorage.
     */
    val freeSpace: Long
        get() = capacity - contents.amount

    /**
     * Describes the contents of this MassStorage in a player-friendly way. Reserve advanced state and implementation
     * details for toString.
     */
    fun describeContents(): Text
}

/**
 * A MutableMassStorage defines a MassStorage that can be modified in-place.
 */
interface MutableMassStorage<T> : MassStorage<T> {
    /**
     * The modifiable contents of this MassStorage.
     */
    override var contents: Quantizer<T>

    /**
     * The modifiable capacity of this MassStorage.
     */
    override var capacity: Long

    /**
     * Adds at most [amount] to this MutableMassStorage's contents, returning the portion that **could not** be
     * inserted.
     *
     * If [simulate] is true, no changes will be made.
     */
    fun addAtMost(amount: Long, simulate: Boolean = false): Long {
        val insertedAmount = if (amount <= freeSpace) amount else freeSpace

        if (!simulate) contents += insertedAmount

        return amount - insertedAmount
    }

    /**
     * Adds the given [quantizer] to this MutableMassStorage's contents, returning the portion that **could not** be
     * inserted (either due to space limitations or an incompatible input).
     *
     * If [simulate] is true, no changes will be made.
     */
    fun addAtMost(quantizer: Quantizer<T>, simulate: Boolean = false): Quantizer<T> {
        if (!quantizer.canMergeWith(contents)) {
            return quantizer
        }

        return quantizer.withAmount(addAtMost(quantizer.amount, simulate))
    }

    /**
     * Adds at most [amount] from this MutableMassStorage's contents, returning the portion that **could** be removed.
     *
     * If [simulate] is true, no changes will be made.
     */
    fun removeAtMost(amount: Long, simulate: Boolean = false): Long {
        val removedAmount = if (amount >= contents.amount) contents.amount else amount

        if (!simulate) contents -= amount

        return removedAmount
    }

    /**
     * Removes the given [quantizer] to this MutableMassStorage's contents, returning the portion that **could** be
     * removed (limited either due to a quantity too small or an incompatible input).
     *
     * If [simulate] is true, no changes will be made.
     */
    fun removeAtMost(quantizer: Quantizer<T>, simulate: Boolean = false): Quantizer<T> {
        if (!quantizer.canMergeWith(contents)) {
            return quantizer.withAmount(0)
        }

        return quantizer.withAmount(removeAtMost(quantizer.amount, simulate))
    }
}
