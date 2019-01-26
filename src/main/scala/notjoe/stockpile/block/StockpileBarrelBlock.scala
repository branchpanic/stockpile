package notjoe.stockpile.block

import java.text.NumberFormat
import java.util

import net.fabricmc.fabric.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipOptions
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.property.Properties
import net.minecraft.text.{Style, TextComponent, TextFormat, TranslatableTextComponent}
import net.minecraft.util.Hand
import net.minecraft.util.hit.{BlockHitResult, HitResult}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.loot.context.{LootContext, Parameters}
import net.minecraft.world.{BlockView, RayTraceContext, World}
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity

import scala.collection.JavaConverters._

object StockpileBarrelBlock extends BlockWithEntity(FabricBlockSettings.copy(Blocks.CHEST).build())
  with FacingDirection {

  val StoredTileTagName = "barrelData"
  val ContentsTextStyle: Style = new Style().setColor(TextFormat.DARK_GRAY)

  override def createBlockEntity(blockView: BlockView): BlockEntity = new StockpileBarrelBlockEntity()

  override def activate(state: BlockState,
                        world: World,
                        pos: BlockPos,
                        player: PlayerEntity,
                        hand: Hand,
                        hitResult: BlockHitResult): Boolean = {
    if (hitResult.getSide != state.get(Properties.FACING)) {
      false
    } else {
      if (!world.isClient) {
        world
          .getBlockEntity(pos)
          .asInstanceOf[StockpileBarrelBlockEntity]
          .handleRightClick(player)
      }
      true
    }
  }

  override def getDroppedStacks(state: BlockState,
                                context: LootContext.Builder): util.List[ItemStack] = {
    val barrelEntity = context
      .get(Parameters.BLOCK_ENTITY)
      .asInstanceOf[StockpileBarrelBlockEntity]

    val stack = new ItemStack(this, 1)

    if (barrelEntity.isInvEmpty) {
      barrelEntity.clearInv()
    }

    stack.setChildTag(StoredTileTagName, barrelEntity.persistentDataToTag())
    List(stack).asJava
  }

  override def onPlaced(world: World,
                        pos: BlockPos,
                        state: BlockState,
                        placer: LivingEntity,
                        stack: ItemStack): Unit = {
    if (!world.isClient) {
      world
        .getBlockEntity(pos)
        .asInstanceOf[StockpileBarrelBlockEntity]
        .loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(StoredTileTagName))
    }
  }

  override def hasComparatorOutput(state: BlockState): Boolean = true

  override def getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int = {
    val barrel = world
      .getBlockEntity(pos)
      .asInstanceOf[StockpileBarrelBlockEntity]

    val amountStored = barrel.inventory.amountStored.toDouble

    if (amountStored <= 0) {
      0
    } else {
      1 + (14 * barrel.inventory.amountStored.toDouble / (barrel.inventory.maxStacks * barrel.inventory.stackSize)).toInt
    }
  }

  override def onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
    if (!world.isClient) {
      val rayTraceStart = player.getCameraPosVec(1)

      // FIXME: Replace 5 with player's actual reach distance
      val rayTraceEnd = rayTraceStart.add(player.getRotationVec(1).multiply(5))

      val result = world.rayTrace(new RayTraceContext(rayTraceStart, rayTraceEnd, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player))

      if (result.getType == HitResult.Type.BLOCK && result.getSide == state.get(Properties.FACING)) {
        world
          .getBlockEntity(pos)
          .asInstanceOf[StockpileBarrelBlockEntity]
          .handleLeftClick(player)
      }
    }
  }

  override def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def getRenderLayer: BlockRenderLayer = BlockRenderLayer.MIPPED_CUTOUT

  def getTooltipForStack(stack: ItemStack): Seq[TextComponent] = {
    val storedEntity = new StockpileBarrelBlockEntity()
    val formatter = NumberFormat.getInstance()

    storedEntity.loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(StoredTileTagName))

    val capacityDescription = new TranslatableTextComponent("stockpile.barrel.capacity", formatter.format(storedEntity.inventory.maxStacks)).setStyle(Description.DefaultStyle)
    val contentDescription = if (storedEntity.isInvEmpty) {
      new TranslatableTextComponent("stockpile.barrel.empty").setStyle(ContentsTextStyle)
    } else {
      new TranslatableTextComponent("stockpile.barrel.contents_stack",
        storedEntity.inventory.stackType.getDisplayName,
        formatter.format(storedEntity.inventory.amountStored),
        formatter.format(storedEntity.inventory.amountStored / storedEntity.inventory.stackSize)
      ).setStyle(ContentsTextStyle)
    }

    Seq(capacityDescription, contentDescription)
  }

  override def buildTooltip(stack: ItemStack,
                            view: BlockView,
                            tooltip: util.List[TextComponent],
                            options: TooltipOptions): Unit = {
    tooltip.addAll(getTooltipForStack(stack).asJava)
  }
}
