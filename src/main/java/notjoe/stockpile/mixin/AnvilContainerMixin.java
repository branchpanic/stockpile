package notjoe.stockpile.mixin;

import net.minecraft.block.Block;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Property;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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

import javax.annotation.Nullable;

@Mixin(AnvilContainer.class)
public abstract class AnvilContainerMixin extends Container {
    private final Block BARREL = Registry.BLOCK.get(new Identifier("stockpile", "barrel"));
    private final double XP_REQUIRED_PER_STACK = 0.07;

    @Shadow
    @Final
    private Inventory result;

    @Shadow
    @Final
    private Inventory inventory;

    @Shadow
    @Final
    private Property pos;

    protected AnvilContainerMixin(@Nullable ContainerType<?> containerType_1, int int_1) {
        super(containerType_1, int_1);
    }

    @Inject(method = "method_7628()V", at = @At("RETURN"))
    private void method_7628(CallbackInfo ci) {
        ItemStack input = inventory.getInvStack(0);
        ItemStack modifier = inventory.getInvStack(1);

        if (!input.getItem().equals(BARREL.getItem()) || input.getAmount() != 1) {
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
        pos.set((int) (XP_REQUIRED_PER_STACK * stacksAddedByModifier));
        sendContentUpdates();
    }

    private int getAddedStacks(ItemStack stack) {
        if (StockpileTags.barrelStorageUpgrade().contains(stack.getItem())) {
            return 27 * stack.getAmount();
        }

        return 0;
    }
}
