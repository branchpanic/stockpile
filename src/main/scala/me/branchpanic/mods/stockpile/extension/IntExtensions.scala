package me.branchpanic.mods.stockpile.extension

object IntExtensions {
  private[this] val SUFFIXES = "kMBT"

  implicit class RichInt(val i: Int) extends AnyVal {
    def shorthand: String = {
      val orderOfMagnitude = Math.log10(i).toInt
      if (orderOfMagnitude < 4) {
        return "%,d".format(i)
      }

      val displayMagnitude = orderOfMagnitude / 3
      val suffix =
        SUFFIXES.charAt(Math.min(displayMagnitude - 1, SUFFIXES.length))
      val displayNumber = "%.1f".format(i / Math.pow(10, 3 * displayMagnitude))

      displayNumber + suffix
    }
  }
}
