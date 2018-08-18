package notjoe.stockpile.mixin;

import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import notjoe.stockpile.block.StockpileBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockHopper.class)
public class BlockHopperMixin {

    @Inject(method = "onBlockActivated", at = @At("HEAD"))
    private void onHopperActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player, EnumHand hand,
                                   EnumFacing face, float x, float y, float z, CallbackInfoReturnable<Boolean> cir) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.getItem() == Blocks.IRON_BARS.asItem() && !world.isRemote) {
            world.setBlockState(pos, StockpileBlocks.Definitions.getGratedHopper().getDefaultState());
            heldStack.setCount(heldStack.getCount() - 1);
            player.inventory.markDirty();
            cir.setReturnValue(true);
        }
    }
}
