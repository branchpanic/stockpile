package me.branchpanic.mods.stockpile.api.storage

/**
 * A MassStorage tracks a large quantity (one long's worth) of a given instance.
 *
 * The type, T, represents the type of object being stored. When T has its own concept of quantity (i.e. ItemStacks
 * have an `amount` field), it is always respected.
 */
interface MassStorage<T> {

    /**
     * The stored quantity associated with this MassStorage. This must never exceed [capacity].
     */
    val amountStored: Long

    /**
     * The maximum possible quantity within this MassStorage. The [amountStored] property must never exceed this.
     */
    val capacity: Long

    /**
     * The current object counted by this MassStorage. If T has its own concept of quantity, it should be set to
     * exactly 1 and instead be tracked with the [amountStored] property.
     */
    val currentInstance: T

    /**
     * Determines whether or not [currentInstance] is set. If this is false, then the next call to [offer] will change
     * currentInstance.
     */
    val instanceIsSet: Boolean

    /**
     * Determines whether or not this MassStorage is empty.
     */
    val isEmpty: Boolean
        get() = amountStored == 0L

    /**
     * Determines whether or not this MassStorage is full.
     */
    val isFull: Boolean
        get() = amountStored == capacity

    /**
     * Tells this MassStorage to clear its [currentInstance] when [isEmpty] becomes true.
     *
     * If this is called when [isEmpty] is already true, [currentInstance] should be immediately cleared.
     */
    fun clearInstanceWhenEmpty()

    /**
     * Tells this MassStorage to retain its [currentInstance] when [isEmpty] becomes true.
     */
    fun retainInstanceWhenEmpty()

    /**
     * Determines whether or not a given object is acceptable by this MassStorage. This will return false if the given
     * [T] does not match [currentInstance] in an implementation-defined manner.
     *
     * This method makes no guarantees regarding capacity.
     */
    fun accepts(t: T): Boolean

    /**
     * Increments the stored amount by at most the given amount.
     *
     * If the entirety of the given amount could not be inserted, then the remainder is returned. Otherwise, zero will
     * be returned.
     */
    fun add(amount: Long, simulate: Boolean = false): Long

    /**
     * Decrements the stored amount by at most the given amount.
     *
     * If the entirety of the given amount could not be removed, then the amount that was actually removed is returned.
     * Otherwise, zero is returned.
     */
    fun remove(amount: Long, simulate: Boolean = false): Long

    /**
     * Attempts to insert the given list of [T]s into this MassStorage. If [T] has its own concept of quantity, it will
     * be respected and used to check against this MassStorage's capacity.
     *
     * The portion of the given [T]s that could not be accepted is returned. The original elements of the list will not
     * be altered.
     */
    fun offer(ts: List<T>, simulate: Boolean = false): List<T>

    /**
     * Attempts to insert one [T] into this MassStorage. If [T] has its own concept of quantity, it will
     * be respected and used to check against this MassStorage's capacity.
     *
     * The portion of the given [T] that could not be accepted is returned. This method will not alter the input,
     * however it may return the same reference.
     */
    fun offer(t: T, simulate: Boolean = false): T

    /**
     * Attempts to remove and return a given quantity of [T] from this MassStorage. If [T] has its own concept of
     * quantity, it will be respected used in the return value.
     *
     * For example, when [T] is an ItemStack and count is at least 30, calling take(amount = 30) will return a list
     * consisting of a single ItemStack with count 30.
     *
     * Conversely, if [T] is a type with no concept of quantity like a String, calling take(amount = 30) will return a
     * list of at most 30 Strings.
     */
    fun take(amount: Long, simulate: Boolean = false): List<T>
}
