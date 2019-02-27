package notjoe.stockpile.extension

object IntExtensions {
  private val Suffixes = "kMBT"

  implicit class RichInt(val i: Int) extends AnyVal {

    def shorthand: String = {
      val orderOfMagnitude = Math.log10(i).toInt
      if (orderOfMagnitude < 4) {
        return "%,d".format(i)
      }

      val displayMagnitude = orderOfMagnitude / 3
      val suffix =
        Suffixes.charAt(Math.min(displayMagnitude - 1, Suffixes.length))
      val displayNumber = "%.1f".format(i / Math.pow(10, 3 * displayMagnitude))

      displayNumber + suffix
    }
  }
}
