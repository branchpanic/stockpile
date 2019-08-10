package me.branchpanic.mods.stockpile.impl.storage

import me.branchpanic.mods.stockpile.api.storage.MutableMassStorage
import me.branchpanic.mods.stockpile.api.storage.Quantizer
import net.minecraft.potion.Potion
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

class MassPotionStorage(override val capacity: Long, override var contents: Quantizer<Potion>) :
    MutableMassStorage<Potion> {

    override fun describeContents(): Text {
        return TranslatableText(
            "ui.stockpile.potion_barrel.contents",
            contents.amount.format(),
            capacity.format(),
            contents.reference.getName("")
        )
    }
}
