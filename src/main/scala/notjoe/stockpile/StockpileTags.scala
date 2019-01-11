package notjoe.stockpile

import net.minecraft.item.Item
import net.minecraft.tag.{ItemTags, Tag}
import net.minecraft.util.Identifier

object StockpileTags {
  private var _barrelStorageUpgrade: Tag[Item] = _
  def barrelStorageUpgrade: Tag[Item] = _barrelStorageUpgrade

  private[stockpile] def initializeAll(): Unit = {
    _barrelStorageUpgrade = new ItemTags.class_3490(new Identifier("stockpile", "barrel_storage_upgrade"))
  }
}
