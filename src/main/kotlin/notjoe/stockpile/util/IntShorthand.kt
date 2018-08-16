package notjoe.stockpile.util

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow


val INT_ABBREVIATION_SUFFIXES = arrayOf("k", "M", "B", "T")

/**
 * Abbreviates an integer to fit within 5 characters.
 */
fun Int.shorthand(): String {
    val orderOfMagnitude = log10(abs(toDouble())).toInt()
    if (orderOfMagnitude < 4) {
        return "%,d".format(this)
    }

    val displayMagnitude = orderOfMagnitude / 3
    val suffix = INT_ABBREVIATION_SUFFIXES[min(displayMagnitude - 1, INT_ABBREVIATION_SUFFIXES.size)]
    val displayNumber = "%.1f".format(this / 10.0.pow(3 * displayMagnitude))

    return displayNumber + suffix
}