package me.branchpanic.mods.stockpile

import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

object StockpileTags {
  private var _barrelStorageUpgrade: Tag[Item] = _

  def barrelStorageUpgrade: Tag[Item] = _barrelStorageUpgrade

  private[stockpile] def initializeAll(): Unit =
    _barrelStorageUpgrade = TagRegistry.item(new Identifier("stockpile", "barrel_storage_upgrade"))
}
