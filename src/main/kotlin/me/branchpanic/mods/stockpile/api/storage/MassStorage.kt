package me.branchpanic.mods.stockpile.api.storage

import net.minecraft.text.Text

@Deprecated("Use Fabric Transfer API (Storage<T>)")
interface MassStorage<T> {
    /**
     * The contents of this MassStorage.
     */
    val contents: Quantifier<T>

    /**
     * The upper bound on this MassStorage's amount.
     */
    val capacity: Long

    /**
     * Determines whether this MassStorage is empty.
     */
    val isEmpty: Boolean
        get() = contents.amount <= 0L

    /**
     * Determines whether this MassStorage is full.
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