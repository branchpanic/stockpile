package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.InventoryFixedWrapper
import net.minecraft.entity.player.PlayerEntity

/**
 * An UnrestrictedInventoryFixedWrapper extends a InventoryFixedWrapper, allowing it to be used by any player.
 */
class UnrestrictedInventoryFixedWrapper(inv: FixedItemInv) : InventoryFixedWrapper(inv) {
    override fun canPlayerUse(player: PlayerEntity?): Boolean = true
}
