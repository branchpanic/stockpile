package notjoe.stockpile.block

import java.text.NumberFormat
import java.util

import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipOptions
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.{Style, TextComponent, TextFormat, TranslatableTextComponent}
import net.minecraft.util.Hand
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.loot.context.{LootContext, Parameters}
import net.minecraft.world.{BlockView, World}
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity

import scala.collection.JavaConverters._

object StockpileBarrelBlock extends BlockWithEntity(Block.Settings.of(Material.WOOD).strength(2.5f, 2.0f)) with FacingDirection {
  final val STORED_DATA_TAG = "barrelData"
  final val TOOLTIP_STYLE = new Style().setColor(TextFormat.DARK_GRAY)

  override def createBlockEntity(blockView: BlockView): BlockEntity = new StockpileBarrelBlockEntity()

  override def activate(state: BlockState,
                        world: World,
                        pos: BlockPos,
                        player: PlayerEntity,
                        hand: Hand,
                        direction: Direction,
                        hitX: Float,
                        hitY: Float,
                        hitZ: Float): Boolean = {
    if (!world.isClient) {
      world.getBlockEntity(pos)
        .asInstanceOf[StockpileBarrelBlockEntity]
        .onRightClick(player)
    }

    true
  }

  override def getDroppedStacks(state: BlockState,
                                context: LootContext.Builder): util.List[ItemStack] = {
    val barrelEntity = context.get(Parameters.BLOCK_ENTITY)
      .asInstanceOf[StockpileBarrelBlockEntity]

    val stack = new ItemStack(this, 1)

    if (!barrelEntity.isInvEmpty) {
      stack.setChildTag(STORED_DATA_TAG, barrelEntity.persistentDataToTag())
    }

    List(stack).asJava
  }

  override def onPlaced(world: World,
                        pos: BlockPos,
                        state: BlockState,
                        placer: LivingEntity,
                        stack: ItemStack): Unit = {
    if (!world.isClient) {
      world.getBlockEntity(pos)
        .asInstanceOf[StockpileBarrelBlockEntity]
        .loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(STORED_DATA_TAG))
    }
  }

  override def onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
    if (!world.isClient) {
      world.getBlockEntity(pos)
        .asInstanceOf[StockpileBarrelBlockEntity]
        .onLeftClick(player)
    }
  }

  // BlockWithEntity sets this to INVISIBLE by default
  override def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def addInformation(stack: ItemStack,
                              blockView: BlockView,
                              information: util.List[TextComponent],
                              options: TooltipOptions): Unit = {
    val barrelEntity = new StockpileBarrelBlockEntity()
    val formatter = NumberFormat.getInstance()

    barrelEntity.loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(STORED_DATA_TAG))

    information.add(new TranslatableTextComponent("stockpile.barrel.capacity",
      formatter.format(barrelEntity.inventory.maxStacks)).setStyle(TOOLTIP_STYLE))

    if (barrelEntity.isInvEmpty) {
      information.add(new TranslatableTextComponent("stockpile.barrel.empty").setStyle(TOOLTIP_STYLE))
      return
    }

    information.add(new TranslatableTextComponent("stockpile.barrel.contents_stack",
      barrelEntity.inventory.stackType.getDisplayName,
      formatter.format(barrelEntity.inventory.amountStored),
      formatter.format(barrelEntity.inventory.amountStored / barrelEntity.inventory.stackSize)
    ).setStyle(TOOLTIP_STYLE))
  }
}
