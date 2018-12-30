package notjoe.stockpile

import net.fabricmc.api.ModInitializer
import net.minecraft.block.Block
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.item.Item
import net.minecraft.item.block.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import notjoe.stockpile.block.CrateBlock
import notjoe.stockpile.blockentity.CrateBlockEntity

object StockpileInitializer extends ModInitializer {
  private final implicit val BLOCKS: Map[String, Block] = Map(
    "crate" -> CrateBlock
  )

  private final implicit val BLOCK_ENTITY_TYPES: Map[String, BlockEntityType[_ <: BlockEntity]] = Map(
    "crate" -> CrateBlockEntity.TYPE
  )

  private def registerAll[T](registryType: Registry[T])(implicit contents: Map[String, T]): Unit = {
    contents
      .map { case (name, o) => (new Identifier("stockpile", name), o) }
      .foreach { case (id, o) => Registry.register(registryType, id, o) }
  }

  override def onInitialize(): Unit = {
    registerAll(Registry.BLOCK)
    registerAll(Registry.BLOCK_ENTITY)
    registerAll(Registry.ITEM)(BLOCKS.mapValues(new BlockItem(_, new Item.Settings())))
  }
}