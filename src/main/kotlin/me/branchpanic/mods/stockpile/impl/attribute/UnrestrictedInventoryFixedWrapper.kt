package me.branchpanic.mods.stockpile.impl.attribute

import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.compat.InventoryFixedWrapper
import net.minecraft.entity.player.PlayerEntity

class UnrestrictedInventoryFixedWrapper(inv: FixedItemInv) : InventoryFixedWrapper(inv) {
    override fun canPlayerUseInv(player: PlayerEntity?): Boolean = true
}
