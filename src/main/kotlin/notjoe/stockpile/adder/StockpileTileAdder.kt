package notjoe.stockpile.adder

import net.minecraft.tileentity.TileEntityType
import notjoe.stockpile.tile.TileBarrel
import org.dimdev.rift.listener.TileEntityTypeAdder

@Suppress("unused")
class StockpileTileAdder : TileEntityTypeAdder {
    override fun registerTileEntityTypes() {
        TileBarrel.TYPE = TileEntityType.registerTileEntityType("barrel", TileEntityType.Builder.create { TileBarrel() })
    }
}