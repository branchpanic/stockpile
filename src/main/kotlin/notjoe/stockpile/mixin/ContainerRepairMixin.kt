package notjoe.stockpile.mixin

import net.minecraft.init.Blocks
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerRepair
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import notjoe.stockpile.block.StockpileBlockAdder.Definitions.BARREL
import notjoe.stockpile.tile.TileBarrel
import notjoe.stockpile.tile.inventory.MutableMassItemStorage
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

const val CHEST_UPGRADE_STACKS = 27

@Mixin(ContainerRepair::class)
abstract class ContainerRepairMixin : Container() {
    @Shadow
    @Final
    private lateinit var outputSlot: IInventory

    @Shadow
    @Final
    private lateinit var inputSlots: IInventory

    @Shadow
    private var maximumCost = 0

    private fun upgradeBarrelStack(barrelStack: ItemStack, addedStacks: Int): ItemStack {
        val upgradedStack = barrelStack.copy()
        upgradedStack.barrelMaxStacks += addedStacks
        return upgradedStack
    }

    @Inject(method = ["updateRepairOutput"], at = [At("RETURN")])
    fun updateRepairOutput(ci: CallbackInfo) {
        val currentInput = inputSlots.getStackInSlot(0)
        val currentModifier = inputSlots.getStackInSlot(1)
        if (currentInput.item != BARREL.item) {
            return
        }

        if (currentModifier.item == Blocks.CHEST.item) {
            val chestCount = currentModifier.count
            outputSlot.setInventorySlotContents(0, upgradeBarrelStack(currentInput, CHEST_UPGRADE_STACKS * chestCount))
            maximumCost = (0.6 * chestCount + 1).toInt()
            detectAndSendChanges()
        }
    }

    private var ItemStack.barrelMaxStacks
        get() = getOrInitBarrelData().getCompoundTag("Inventory").getInteger(MutableMassItemStorage.MAX_STACKS_KEY)
        set(value) = getOrInitBarrelData().getCompoundTag("Inventory").setInteger(MutableMassItemStorage.MAX_STACKS_KEY, value)

    private fun ItemStack.getOrInitBarrelData(): NBTTagCompound {
        val stackCompound = func_196082_o()

        if (!stackCompound.hasKey("BarrelTileData")) {
            stackCompound.setTag("BarrelTileData", TileBarrel().writePersistentValuesToNBT(NBTTagCompound()))
        }

        return stackCompound.getCompoundTag("BarrelTileData")
    }
}
