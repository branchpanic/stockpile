package notjoe.stockpile.util

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow


val SHORTHAND_SUFFIXES = arrayOf("k", "M", "B", "T")
fun Int.shorthand(): String {
    val orderOfMagnitude = log10(abs(toDouble())).toInt() - 1
    if (orderOfMagnitude < 3) {
        return toString()
    }

    val suffix = SHORTHAND_SUFFIXES[min(orderOfMagnitude % 3, SHORTHAND_SUFFIXES.size)]
    val displayNumber = "%.2f".format(this / 10.0.pow(orderOfMagnitude))

    return displayNumber + suffix
}