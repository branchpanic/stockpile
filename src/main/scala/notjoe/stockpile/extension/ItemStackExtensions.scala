package notjoe.stockpile.extension

import net.minecraft.item.ItemStack

object ItemStackExtensions {
  implicit class RichItemStack(val stack: ItemStack) extends AnyVal {
    def withAmount(newCount: Int): ItemStack = {
      val copy = stack.copy()
      copy.setAmount(newCount)
      copy
    }
  }
}
