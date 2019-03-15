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
  val ITEM_GROUP: ItemGroup =
    FabricItemGroupBuilder.build(new Identifier("stockpile", "all"), () => new ItemStack(StockpileBarrelBlock))

  private val BLOCKS: Map[String, Block] = Map(
    "barrel" -> StockpileBarrelBlock,
    "trash_can" -> TrashCanBlock
  )

  private val BLOCK_ENTITY_TYPES: Map[String, BlockEntityType[_ <: BlockEntity]] = Map(
    "barrel" -> StockpileBarrelBlockEntity.TYPE,
    "trash_can" -> TrashCanBlockEntity.TYPE
  )

  override def onInitialize(): Unit = {
    registerAll(Registry.BLOCK, BLOCKS)
    registerAll(Registry.BLOCK_ENTITY, BLOCK_ENTITY_TYPES)
    registerAll(Registry.ITEM, BLOCKS.mapValues(new BlockItem(_, new Item.Settings().itemGroup(ITEM_GROUP))))

    StockpileTags.initializeAll()
  }

  private def registerAll[T](registryType: Registry[T], contents: Map[String, T]): Unit = {
    contents
      .map { case (name, o) => (new Identifier("stockpile", name), o) }
      .foreach { case (id, o) => Registry.register(registryType, id, o) }
  }
}
