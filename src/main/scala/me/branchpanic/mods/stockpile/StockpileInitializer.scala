package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.block.{StockpileBarrelBlock, TrashCanBlock}
import me.branchpanic.mods.stockpile.blockentity.{StockpileBarrelBlockEntity, TrashCanBlockEntity}
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.item.block.BlockItem
import net.minecraft.item.{Item, ItemGroup, ItemStack}
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object StockpileInitializer extends ModInitializer {

  val ItemGroup: ItemGroup =
    FabricItemGroupBuilder.build(new Identifier("stockpile", "all"),
                                 () => new ItemStack(StockpileBarrelBlock))

  private implicit val Blocks: Map[String, Block] = Map(
    "barrel" -> StockpileBarrelBlock,
    "trash_can" -> TrashCanBlock
  )

  private implicit val BlockEntityTypes: Map[String, BlockEntityType[_ <: BlockEntity]] = Map(
    "barrel" -> StockpileBarrelBlockEntity.Type,
    "trash_can" -> TrashCanBlockEntity.Type
  )

  override def onInitialize(): Unit = {
    registerAll(Registry.BLOCK)
    registerAll(Registry.BLOCK_ENTITY)
    registerAll(Registry.ITEM)(
      Blocks.mapValues(new BlockItem(_, new Item.Settings().itemGroup(ItemGroup))))

    StockpileTags.initializeAll()
  }

  private def registerAll[T](registryType: Registry[T])(implicit contents: Map[String, T]): Unit = {
    contents
      .map { case (name, o) => (new Identifier("stockpile", name), o) }
      .foreach { case (id, o) => Registry.register(registryType, id, o) }
  }
}
