package notjoe.stockpile.mixin;

import net.minecraft.block.Block;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import notjoe.stockpile.StockpileTags;
import notjoe.stockpile.block.StockpileBarrelBlock;
import notjoe.stockpile.blockentity.StockpileBarrelBlockEntity;
import notjoe.stockpile.inventory.MassItemInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilContainer.class)
public abstract class AnvilContainerMixin extends Container {
    private final Block BARREL = Registry.BLOCK.get(new Identifier("stockpile", "barrel"));

    @Shadow
    public int field_7772;

    @Shadow
    @Final
    private Inventory result;

    @Shadow
    @Final
    private Inventory inventory;

    @Shadow @Final private World world;

    @Inject(method = "method_7628()V", at = @At("RETURN"))
    private void method_7628(CallbackInfo ci) {
        if (world.isClient) {
            return;
        }

        ItemStack input = inventory.getInvStack(0);
        ItemStack modifier = inventory.getInvStack(1);

        if (!input.getItem().equals(BARREL.getItem())) {
            return;
        }

        int stacksAddedByModifier = getAddedStacks(modifier);

        if (stacksAddedByModifier <= 0) {
            return;
        }

        StockpileBarrelBlockEntity barrelInfo = new StockpileBarrelBlockEntity();
        barrelInfo.loadPersistentDataFromTag(input.getOrCreateSubCompoundTag(StockpileBarrelBlock.StoredTileTagName()));
        int currentStackLimit = barrelInfo.inventory().maxStacks();

        if (currentStackLimit + stacksAddedByModifier > MassItemInventory.MaxCapacityStacks()) {
            return;
        }

        barrelInfo.inventory().maxStacks_$eq(currentStackLimit + stacksAddedByModifier);

        ItemStack upgradedBarrel = input.copy();
        upgradedBarrel.setChildTag(StockpileBarrelBlock.StoredTileTagName(), barrelInfo.persistentDataToTag());

        result.setInvStack(0, upgradedBarrel);
        field_7772 = (int) (0.3 * stacksAddedByModifier);
        sendContentUpdates();
    }

    private int getAddedStacks(ItemStack stack) {
        if (StockpileTags.barrelStorageUpgrade().contains(stack.getItem())) {
            return 27 * stack.getAmount();
        }

        return 0;
    }
}
