package me.branchpanic.mods.stockpile.api.storage

@Deprecated("Use Fabric Transfer API (TransferVariant<O>)")
interface Quantifier<T> {
    /**
     * Object counted by this Quantifier.
     */
    val reference: T

    /**
     * Represented quantity of the stored object.
     */
    val amount: Long

    /**
     * Returns a copy of this Quantifier with the given amount.
     */
    fun withAmount(amount: Long): Quantifier<T>

    /**
     * Determines whether this Quantifier is empty.
     */
    val isEmpty: Boolean
        get() = amount <= 0

    /**
     * Determines whether two Quantifiers are effectively equal.
     *
     * If this is true of Quantifiers A and B, then a Quantifier C with `reference == A.reference` and
     * `amount == A.amount + B.amount` is a completely accurate combination of A and B.
     */
    fun canMergeWith(other: Quantifier<T>): Boolean {
        return false
    }

    /**
     * Unwraps this Quantifier into a collection of Objects.
     */
    fun toObjects(): List<T>

    /**
     * Returns a new Quantifier with the given amount added.
     */
    operator fun plus(amount: Long): Quantifier<T> {
        require(this.amount + amount >= 0) {
            "Attempted to add $amount to a Quantifier representing ${this.amount} objects, which would result in a " +
                    "negative quantity."
        }
        return withAmount(this.amount + amount)
    }

    /**
     * Returns a new Quantifier with the contents of another added. This method must be implemented manually, so it can
     * respect the following:
     *
     *      Given:
     *          T_0 = an "empty" T, i.e. ItemStack.EMPTY
     *          T_1 = any unique "non-empty" T, i.e. an ItemStack of 1x stone
     *          X of Y = a Quantifier with reference Y and amount X
     *      Then:
     *          n of T_0 + m of T_1 = (n + m) of T_1
     *      However, note that:
     *          n of T_1 - n of T_1 = 0 of T_1, not 0 of T_0
     *
     * @throws [IllegalArgumentException] if canMergeWith(other) is false.
     */
    operator fun plus(other: Quantifier<T>): Quantifier<T> {
        require(canMergeWith(other)) {
            "Attempted to merge two Quantized objects ($this + $other) that can't be merged."
        }
        return this + other.amount
    }

    /**
     * Returns a new Quantifier with the given amount subtracted.
     */
    operator fun minus(amount: Long): Quantifier<T> {
        require(this.amount - amount >= 0) {
            "Attempted to subtract $amount from a Quantifier representing ${this.amount} objects, which would result " +
                    "in a negative quantity."
        }
        return withAmount(this.amount - amount)
    }

    /**
     * Returns a new Quantifier with the contents of another subtracted.
     *
     * @throws [IllegalArgumentException] if canMergeWith(other) is false.
     */
    operator fun minus(other: Quantifier<T>): Quantifier<T> {
        require(canMergeWith(other)) {
            "Attempted to merge two Quantized objects ($this + $other) that can't be merged."
        }
        return this - other.amount
    }
}
