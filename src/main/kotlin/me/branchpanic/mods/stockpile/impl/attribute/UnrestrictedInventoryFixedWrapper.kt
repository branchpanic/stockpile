package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.InventoryFixedWrapper
import net.minecraft.entity.player.PlayerEntity

@Deprecated("No longer needed")
class UnrestrictedInventoryFixedWrapper(inv: FixedItemInv) : InventoryFixedWrapper(inv) {
    override fun canPlayerUse(player: PlayerEntity?): Boolean = true
}
