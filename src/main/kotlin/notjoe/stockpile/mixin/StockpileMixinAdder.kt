package notjoe.stockpile.mixin

import org.dimdev.riftloader.listener.InitializationListener
import org.spongepowered.asm.mixin.Mixins

class StockpileMixinAdder : InitializationListener {
    override fun onInitialization() {
        Mixins.addConfiguration("mixins.stockpile.json")
    }
}