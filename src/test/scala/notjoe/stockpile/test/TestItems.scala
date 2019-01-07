package notjoe.stockpile.test

import net.minecraft.item.{Item, Items}
import org.powermock.reflect.Whitebox

object TestItems {
  // We need to make sure AIR is set so methods like ItemStack::isEmpty work properly
  Whitebox.setInternalState(classOf[Items], "AIR", new Item(new Item.Settings()).asInstanceOf[Any])

  val Red = new Item(new Item.Settings())
  val Blue = new Item(new Item.Settings())
}
