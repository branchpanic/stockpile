package me.branchpanic.mods.stockpile.content.client.renderer

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

data class FillBarSettings(
    val backgroundColor: ArgbColor,
    val foregroundColor: ArgbColor,
    val textColor: ArgbColor,
    val textColorFull: ArgbColor,
    val width: Double
)

// TODO(i18n): Localize quantities
val NUMBER_SUFFIXES = arrayOf("k", "M", "B", "T", "Q")

fun Long.abbreviate(): String {
    val orderOfMagnitude = log10(abs(toDouble())).toInt()
    if (orderOfMagnitude < 4) {
        return "%,d".format(this)
    }

    val displayMagnitude = orderOfMagnitude / 3
    val suffix = NUMBER_SUFFIXES[min(displayMagnitude - 1, NUMBER_SUFFIXES.size)]
    val displayNumber = "%.1f".format(this / 10.0.pow(3 * displayMagnitude))

    return displayNumber + suffix
}
