@file:Suppress("OverridingDeprecatedMember")

package notjoe.stockpile.block

import net.minecraft.block.Block
import net.minecraft.block.BlockDirectional
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import notjoe.stockpile.tile.AbstractBaseTileEntity
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.tile.inventory.MutableMassItemStorage
import notjoe.stockpile.util.rayTraceFromEyes

class BlockBarrel(private val maxStacks: Int = 32) :
        BlockDirectional(Block.Builder
                .create(Material.WOOD)
                .soundType(SoundType.WOOD)
                .hardnessAndResistance(3f, 14f)), ITileEntityProvider {

    init {
        defaultState = blockState.baseState.withProperty(FACING, EnumFacing.NORTH)
    }


    override fun addPropertiesToBuilder(stateBuilder: StateContainer.Builder<Block, IBlockState>?) {
        stateBuilder?.addProperties(FACING)
    }

    override fun hasTileEntity(): Boolean = true
    override fun getTileEntity(p0: IBlockReader?): TileEntity? = TileBarrel(maxStacks)

    override fun onLeftClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?) {
        if (world == null || player == null || state == null || world.isRemote) {
            return
        }

        val rayTraceResult = player.rayTraceFromEyes(4.0)

        if (rayTraceResult == null || rayTraceResult.sideHit != state.getValue(FACING)) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleLeftClick(player)
    }

    @Suppress("deprecated")
    override fun onRightClick(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?,
                              hand: EnumHand?, face: EnumFacing?, x: Float, y: Float, z: Float): Boolean {
        if (world == null || player == null || world.isRemote) {
            return true
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleRightClick(player)

        return true
    }

    override fun getBlockToPlaceOnUse(context: BlockItemUseContext?): IBlockState? {
        if (context == null) {
            return null
        }

        return defaultState.withProperty(FACING, context.func_196010_d().opposite)
    }

    override fun beforeReplacingBlock(oldState: IBlockState?, world: World?, pos: BlockPos?, newState: IBlockState?, unknown: Boolean) {
        if (world == null || pos == null || oldState == newState || world.isRemote) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel

        if (tile.isEmpty) {
            return
        }

        val workingStack = ItemStack(item)
        val tileDataCompound = tile.writePersistentValuesToNBT(NBTTagCompound())
        workingStack.setTagInfo("BarrelTileData", tileDataCompound)

        Block.spawnAsEntity(world, pos, workingStack)
    }

    override fun spawnItems(state: IBlockState?, world: World?, pos: BlockPos?, p_spawnItems_4_: Float, p_spawnItems_5_: Int) {
        if (world == null || pos == null || world.isRemote) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel

        if (tile.isEmpty) {
            Block.spawnAsEntity(world, pos, ItemStack(item))
        }
    }

    override fun onBlockPlacedBy(world: World?, pos: BlockPos?, state: IBlockState?, placer: EntityLivingBase?,
                                 placeStack: ItemStack?) {
        if (world == null || pos == null || placeStack == null || world.isRemote) {
            return
        }

        val stackCompound = placeStack.func_196082_o()
        if (stackCompound.hasKey("BarrelTileData")) {
            val tile = world.getTileEntity(pos) as AbstractBaseTileEntity
            tile.readPersistentValuesFromNBT(stackCompound.getCompoundTag("BarrelTileData"))
        }
    }

    override fun addInformation(stack: ItemStack?, world: IBlockReader?, textComponents: MutableList<ITextComponent>?, tooltipFlag: ITooltipFlag?) {
        if (stack == null || textComponents == null) {
            return
        }

        val stackCompound = stack.func_196082_o()
        if (stackCompound.hasKey("BarrelTileData")) {
            val inventoryCompound = stackCompound.getCompoundTag("BarrelTileData").getCompoundTag("Inventory")

            val stackType = ItemStack.func_199557_a(inventoryCompound.getCompoundTag(
                    MutableMassItemStorage.STACK_TYPE_KEY))
            val amount = inventoryCompound.getInteger(MutableMassItemStorage.AMOUNT_KEY)

            val contentsComponent = TextComponentTranslation("stockpile.barrel.contents_stack",
                    String.format("%,d", amount), stackType.item.getDisplayName(stackType),
                    String.format("%,d", amount / stackType.maxStackSize))
            contentsComponent.style.apply {
                color = TextFormatting.YELLOW
            }

            textComponents.add(contentsComponent)
        }

        val sizeComponent = TextComponentTranslation("stockpile.barrel.size_stack", maxStacks)
        sizeComponent.style.apply {
            color = TextFormatting.DARK_GRAY
        }

        textComponents.add(sizeComponent)
    }
}