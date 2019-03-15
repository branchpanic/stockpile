package me.branchpanic.mods.stockpile.block

import java.util

import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.{Style, TextComponent, TextFormat, TranslatableTextComponent}
import net.minecraft.world.BlockView

object BlockDescription {
  val DEFAULT_STYLE: Style = new Style().setColor(TextFormat.GRAY)
}

trait BlockDescription extends Block {
  override def buildTooltip(stack: ItemStack,
                            view: BlockView,
                            tooltip: util.List[TextComponent],
                            context: TooltipContext): Unit = {
    tooltip.add(
      new TranslatableTextComponent(stack.getTranslationKey + ".description")
        .setStyle(BlockDescription.DEFAULT_STYLE))

    super.buildTooltip(stack, view, tooltip, context)
  }
}
