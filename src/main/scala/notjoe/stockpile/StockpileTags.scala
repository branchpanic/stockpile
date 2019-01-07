package notjoe.stockpile

import net.minecraft.item.Item
import net.minecraft.tag.{ItemTags, Tag}
import net.minecraft.util.Identifier

object StockpileTags {
  var _barrelUpgrade27: Tag[Item] = _
  def barrelUpgrade27: Tag[Item] = _barrelUpgrade27

  private[stockpile] def initializeAll(): Unit = {
    _barrelUpgrade27 = new ItemTags.class_3490(new Identifier("stockpile", "barrel_upgrade_27"))
  }
}
