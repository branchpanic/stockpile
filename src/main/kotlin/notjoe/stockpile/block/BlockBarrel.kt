@file:Suppress("OverridingDeprecatedMember", "DEPRECATION")

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
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.util.ext.rayTraceFromEyes

class BlockBarrel :
    BlockDirectional(
        Block.Builder
            .create(Material.WOOD)
            .sound(SoundType.WOOD)
            .hardnessAndResistance(3f, 14f)
    ), ITileEntityProvider {

    init {
        defaultState = blockState.baseState.withProperty(FACING, EnumFacing.NORTH)
    }

    override fun addPropertiesToBuilder(stateBuilder: StateContainer.Builder<Block, IBlockState>?) {
        stateBuilder?.add(FACING)
    }

    override fun hasTileEntity(): Boolean = true
    override fun createNewTileEntity(world: IBlockReader?): TileEntity? = TileBarrel()

    override fun hasComparatorInputOverride(state: IBlockState?): Boolean = true
    override fun getComparatorInputOverride(state: IBlockState?, world: World?, pos: BlockPos?): Int {
        if (world == null || pos == null) {
            return 0
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        return (15 * (tile.amountStored.toDouble() / (tile.maxStacks * tile.stackType.maxStackSize))).toInt()
    }

    override fun onBlockClicked(state: IBlockState?, world: World?, pos: BlockPos?, player: EntityPlayer?) {
        if (world == null || player == null || state == null || world.isRemote) {
            return
        }

        val rayTraceResult = player.rayTraceFromEyes(5.0)

        if (rayTraceResult == null || rayTraceResult.sideHit != state.getValue(FACING)) {
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleLeftClick(player)
    }

    override fun onBlockActivated(
        state: IBlockState?,
        world: World?,
        pos: BlockPos?,
        player: EntityPlayer?,
        hand: EnumHand?,
        face: EnumFacing?,
        x: Float,
        y: Float,
        z: Float
    ): Boolean {
        if (world == null || player == null || world.isRemote) {
            return true
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        tile.handleRightClick(player)

        return true
    }

    override fun getStateForPlacement(context: BlockItemUseContext?): IBlockState? {
        if (context == null) {
            return null
        }

        return defaultState.withProperty(FACING, context.func_196010_d().opposite)
    }

    override fun onReplaced(
        oldState: IBlockState?,
        world: World?,
        pos: BlockPos?,
        newState: IBlockState?,
        unknown: Boolean
    ) {
        if (world == null || pos == null || oldState?.block == newState?.block || world.isRemote) {
            return
        }

        world.removeTileEntity(pos)
    }

    override fun onBlockHarvested(world: World?, pos: BlockPos?, state: IBlockState?, player: EntityPlayer?) {
        if (world == null || pos == null || player == null || player.isCreative) {
            super.onBlockHarvested(world, pos, state, player)
            return
        }

        val tile = world.getTileEntity(pos) as TileBarrel
        val workingStack = ItemStack(asItem())

        if (tile.isEmpty) {
            tile.clearStackType()
            tile.markDirty()
        }

        val tileDataCompound = tile.writePersistentValuesToNBT(NBTTagCompound())
        workingStack.setTagInfo("BarrelTileData", tileDataCompound)

        Block.spawnAsEntity(world, pos, workingStack)

        super.onBlockHarvested(world, pos, state, player)
    }

    override fun dropBlockAsItemWithChance(
        state: IBlockState?,
        world: World?,
        pos: BlockPos?,
        p_spawnItems_4_: Float,
        p_spawnItems_5_: Int
    ) {
        // NO-OP: Instead of spawning an item here, a version containing the TileEntity data is spawned in
        //        beforeReplacingBlock.
    }

    override fun onBlockPlacedBy(
        world: World?,
        pos: BlockPos?,
        state: IBlockState?,
        placer: EntityLivingBase?,
        placeStack: ItemStack?
    ) {
        if (world == null || pos == null || placeStack == null || world.isRemote) {
            return
        }

        val stackCompound = placeStack.orCreateTagCompound
        if (stackCompound.hasKey("BarrelTileData")) {
            val tile = world.getTileEntity(pos) as TileBarrel
            tile.readPersistentValuesFromNBT(stackCompound.getCompoundTag("BarrelTileData"))
        }
    }

    override fun getItem(world: IBlockReader?, pos: BlockPos?, state: IBlockState?): ItemStack {
        if (world == null || pos == null || state == null) {
            return ItemStack.EMPTY
        }

        val tile = world.getTileEntity(pos) as TileBarrel

        val tileDataCompound = tile.writePersistentValuesToNBT(NBTTagCompound())
        return super.getItem(world, pos, state).apply { setTagInfo("BarrelTileData", tileDataCompound) }
    }

    override fun addInformation(
        stack: ItemStack?,
        world: IBlockReader?,
        lore: MutableList<ITextComponent>?,
        tooltipFlag: ITooltipFlag?
    ) {
        if (stack == null || lore == null) {
            return
        }

        val storedTile = try {
            TileBarrel()
        } catch (e: Exception) {
            return
        }

        storedTile.readPersistentValuesFromNBT(stack.orCreateTagCompound.getCompoundTag("BarrelTileData"))

        if (storedTile.isEmpty) {
            val emptyComponent = TextComponentTranslation("stockpile.barrel.empty")
            emptyComponent.style.color = TextFormatting.GRAY
            lore.add(emptyComponent)
        } else {
            val containedItem = storedTile.stackType
            val containedItemName = containedItem.item.name.unformattedComponentText
            val containedAmount = storedTile.amountStored
            val containedStacks = containedAmount / containedItem.maxStackSize
            val stacksContainedComponent = TextComponentTranslation(
                "stockpile.barrel.contents_stack",
                containedItemName, "%,d".format(containedAmount), "%,d".format(containedStacks)
            )
            stacksContainedComponent.style.color = TextFormatting.GRAY
            lore.add(stacksContainedComponent)
        }

        val stackSizeComponent = TextComponentTranslation("stockpile.barrel.size_stack", storedTile.maxStacks)
        stackSizeComponent.style.color = TextFormatting.DARK_GRAY
        lore.add(stackSizeComponent)
    }
}