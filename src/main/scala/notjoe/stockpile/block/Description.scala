package notjoe.stockpile.block

import java.util

import net.minecraft.block.Block
import net.minecraft.client.item.TooltipOptions
import net.minecraft.item.ItemStack
import net.minecraft.text.{Style, TextComponent, TextFormat, TranslatableTextComponent}
import net.minecraft.world.BlockView

object Description {
  val DefaultStyle: Style = new Style().setColor(TextFormat.GRAY)
}

trait Description extends Block {
  val descriptionStyle: Style = Description.DefaultStyle

  override def buildTooltip(stack: ItemStack,
                              view: BlockView,
                              tooltip: util.List[TextComponent],
                              options: TooltipOptions): Unit = {
    tooltip.add(new TranslatableTextComponent(stack.getTranslationKey + ".description").setStyle(descriptionStyle))
    super.buildTooltip(stack, view, tooltip, options)
  }
}
