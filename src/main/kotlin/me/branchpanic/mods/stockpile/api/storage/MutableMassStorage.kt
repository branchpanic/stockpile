package me.branchpanic.mods.stockpile.api.storage

@Deprecated("Use Fabric Transfer API (Storage<T>)")
interface MutableMassStorage<T> : MassStorage<T> {
    /**
     * The modifiable contents of this MassStorage.
     */
    override var contents: Quantifier<T>

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
     * Adds the given [quantifier] to this MutableMassStorage's contents, returning the portion that **could not** be
     * inserted (either due to space limitations or an incompatible input).
     *
     * If [simulate] is true, no changes will be made.
     */
    fun addAtMost(quantifier: Quantifier<T>, simulate: Boolean = false): Quantifier<T> {
        if (!quantifier.canMergeWith(contents)) {
            return quantifier
        }

        val amount = quantifier.amount
        val insertedAmount = if (amount <= freeSpace) amount else freeSpace

        if (!simulate) contents += quantifier.withAmount(insertedAmount)

        return quantifier.withAmount(amount - insertedAmount)
    }

    /**
     * Adds at most [amount] from this MutableMassStorage's contents, returning the portion that **could** be removed.
     *
     * If [simulate] is true, no changes will be made.
     */
    fun removeAtMost(amount: Long, simulate: Boolean = false): Long {
        val removedAmount = if (amount >= contents.amount) contents.amount else amount

        if (!simulate) contents -= removedAmount

        return removedAmount
    }

    /**
     * Removes the given [quantifier] to this MutableMassStorage's contents, returning the portion that **could** be
     * removed (limited either due to a quantity too small or an incompatible input).
     *
     * If [simulate] is true, no changes will be made.
     */
    fun removeAtMost(quantifier: Quantifier<T>, simulate: Boolean = false): Quantifier<T> {
        if (!quantifier.canMergeWith(contents)) {
            return quantifier.withAmount(0)
        }

        return quantifier.withAmount(removeAtMost(quantifier.amount, simulate))
    }
}
