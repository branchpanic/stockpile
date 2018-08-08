package notjoe.stockpile.mixin

import org.dimdev.riftloader.listener.InitializationListener
import org.spongepowered.asm.mixin.Mixins

class StockpileMixins : InitializationListener {
    override fun onInitialization() {
        Mixins.addConfiguration("mixins.stockpile.json")
    }
}