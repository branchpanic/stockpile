package me.branchpanic.mods.stockpile.api.storage

/**
 * A Quantizer standardizes objects with a discrete quantity (i.e. ItemStacks). Quantizers may represent a quantity of
 * zero, but will never represent negative values.
 */
interface Quantizer<T> {
    /**
     * Object counted by this Quantizer.
     */
    val reference: T

    /**
     * Represented amount of the stored object.
     */
    val amount: Long

    /**
     * Returns a copy of this Quantizer with the given amount.
     */
    fun withAmount(amount: Long): Quantizer<T>

    /**
     * Determines whether or not this Quantizer is empty.
     */
    val isEmpty: Boolean
        get() = amount <= 0

    /**
     * Determines whether or not two Quantizers are effectively equal.
     *
     * If this is true of quantizers A and B, then a quantizer C with `reference == A.reference` and
     * `amount == A.amount + B.amount` is a completely accurate combination of A and B.
     */
    fun canMergeWith(other: Quantizer<T>): Boolean {
        return false
    }

    /**
     * Unwraps this Quantizer into a collection of Objects.
     */
    fun toObjects(): List<T>

    /**
     * Returns a new Quantizer with the given amount added.
     */
    operator fun plus(amount: Long): Quantizer<T> {
        require(this.amount + amount >= 0) {
            "Attempted to add $amount to a Quantizer representing ${this.amount} objects, which would result in a " +
                    "negative quantity."
        }
        return withAmount(this.amount + amount)
    }

    /**
     * Returns a new Quantizer with the contents of another added. This method must be implemented manually so it can
     * respect the following:
     *
     *      Given:
     *          T_0 = an "empty" T, i.e. ItemStack.EMPTY
     *          T_1 = any unique "non-empty" T, i.e. an ItemStack of 1x stone
     *          X of Y = a quantizer with reference Y and amount X
     *      Then:
     *          n of T_0 + m of T_1 = (n + m) of T_1
     *      However, note that:
     *          n of T_1 - n of T_1 = 0 of T_1, not 0 of T_0
     *
     * @throws [IllegalArgumentException] if canMergeWith(other) is false.
     */
    operator fun plus(other: Quantizer<T>): Quantizer<T> {
        require(canMergeWith(other)) {
            "Attempted to merge two Quantized objects ($this + $other) that can't be merged."
        }
        return this + other.amount
    }

    /**
     * Returns a new Quantizer with the given amount subtracted.
     */
    operator fun minus(amount: Long): Quantizer<T> {
        require(this.amount - amount >= 0) {
            "Attempted to subtract $amount from a Quantizer representing ${this.amount} objects, which would result " +
                    "in a negative quantity."
        }
        return withAmount(this.amount - amount)
    }

    /**
     * Returns a new Quantizer with the contents of another subtracted.
     *
     * @throws [IllegalArgumentException] if canMergeWith(other) is false.
     */
    operator fun minus(other: Quantizer<T>): Quantizer<T> {
        require(canMergeWith(other)) {
            "Attempted to merge two Quantized objects ($this + $other) that can't be merged."
        }
        return this - other.amount
    }
}
