package notjoe.stockpile.tile

import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import notjoe.stockpile.renderer.BarrelRenderer
import org.dimdev.rift.listener.TileEntityTypeAdder
import org.dimdev.rift.listener.client.TileEntityRendererAdder

@Suppress("unused")
class StockpileTileEntities : TileEntityTypeAdder, TileEntityRendererAdder {
    override fun registerTileEntityTypes() {
        TileBarrel.TYPE = TileEntityType.registerTileEntityType("stockpile:barrel", TileEntityType.Builder.create { TileBarrel() })
        TileTrashCan.TYPE = TileEntityType.registerTileEntityType("stockpile:trashCan", TileEntityType.Builder.create { TileTrashCan() })
    }

    override fun addTileEntityRenderers(renderers: MutableMap<Class<out TileEntity>, TileEntityRenderer<out TileEntity>>?) {
        renderers?.put(TileBarrel::class.java, BarrelRenderer())
    }
}