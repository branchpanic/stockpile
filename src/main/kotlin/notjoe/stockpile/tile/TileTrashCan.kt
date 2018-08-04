package notjoe.stockpile.tile

import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.ISidedInventory
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import notjoe.stockpile.block.BlockTrashCan
import notjoe.stockpile.tile.inventory.VoidInventory

/**
 * A TileEntity which destroys items inserted into it.
 */
class TileTrashCan : AbstractBaseTileEntity(TYPE), ISidedInventory by VoidInventory(), ITickable {
    companion object Type {
        lateinit var TYPE: TileEntityType<TileTrashCan>
    }

    override fun update() {
        if (world.isRemote || world.getBlockState(pos).block !is BlockTrashCan || !blockState.getValue(BlockTrashCan.LID_OPEN)) {
            return
        }

        world.getEntitiesWithinAABB(EntityItem::class.java, AxisAlignedBB(pos.up())).forEach { entityItem ->
            entityItem.setDead()
        }
    }

    @Suppress("redundant")
    override fun markDirty() = super.markDirty()
}