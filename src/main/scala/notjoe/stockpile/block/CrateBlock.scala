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
import notjoe.stockpile.blockentity.CrateBlockEntity
import notjoe.stockpile.inventory.MassItemInventory

import scala.collection.JavaConverters._

object CrateBlock extends BlockWithEntity(Block.Settings.of(Material.WOOD).strength(2.5f, 2.0f)) with FacingDirection {
  final val STORED_DATA_TAG = "CrateData"
  final val TOOLTIP_STYLE = new Style().setColor(TextFormat.DARK_GRAY)

  override def createBlockEntity(blockView: BlockView): BlockEntity = new CrateBlockEntity()

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
        .asInstanceOf[CrateBlockEntity]
        .onRightClick(player)
    }

    true
  }

  override def getDroppedStacks(state: BlockState,
                                context: LootContext.Builder): util.List[ItemStack] = {
    val crateEntity = context.get(Parameters.BLOCK_ENTITY)
      .asInstanceOf[CrateBlockEntity]

    val stack = new ItemStack(this, 1)

    if (!crateEntity.isInvEmpty) {
      stack.setChildTag(STORED_DATA_TAG, crateEntity.persistentDataToTag())
    }

    List(stack).asJava
  }

  override def onPlaced(world: World,
                        pos: BlockPos,
                        state: BlockState,
                        placer: LivingEntity,
                        stack: ItemStack): Unit = {
    if (!world.isClient) {
      try {
        world.getBlockEntity(pos)
          .asInstanceOf[CrateBlockEntity]
          .loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(STORED_DATA_TAG))
      } catch {
        case _: IllegalArgumentException => // NO-OP. There was nothing valid to load, so we didn't load it.
      }
    }
  }

  override def onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
    if (!world.isClient) {
      world.getBlockEntity(pos)
        .asInstanceOf[CrateBlockEntity]
        .onLeftClick(player)
    }
  }

  // BlockWithEntity sets this to INVISIBLE by default
  override def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def addInformation(stack: ItemStack,
                              blockView: BlockView,
                              information: util.List[TextComponent],
                              options: TooltipOptions): Unit = {
    val crateEntity = new CrateBlockEntity()
    val formatter = NumberFormat.getInstance()

    try {
      crateEntity.loadPersistentDataFromTag(stack.getOrCreateSubCompoundTag(STORED_DATA_TAG))
    } catch {
      case _: IllegalArgumentException =>
        information.add(new TranslatableTextComponent("stockpile.crate.capacity",
          formatter.format(MassItemInventory.DEFAULT_MAX_STACKS)).setStyle(TOOLTIP_STYLE))
        information.add(new TranslatableTextComponent("stockpile.crate.empty").setStyle(TOOLTIP_STYLE))
        return
    }


    information.add(new TranslatableTextComponent("stockpile.crate.capacity",
      formatter.format(crateEntity.inventory.maxStacks)).setStyle(TOOLTIP_STYLE))

    if (crateEntity.isInvEmpty) {
      information.add(new TranslatableTextComponent("stockpile.crate.empty").setStyle(TOOLTIP_STYLE))
      return
    }

    information.add(new TranslatableTextComponent("stockpile.crate.contents_stack",
      crateEntity.inventory.stackType.getDisplayName,
      formatter.format(crateEntity.inventory.amountStored),
      formatter.format(crateEntity.inventory.amountStored / crateEntity.inventory.stackSize)
    ).setStyle(TOOLTIP_STYLE))
  }
}
