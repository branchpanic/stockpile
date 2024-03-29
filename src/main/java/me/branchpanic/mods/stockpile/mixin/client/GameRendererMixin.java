package me.branchpanic.mods.stockpile.mixin.client;

import me.branchpanic.mods.stockpile.client.StockpileClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Adapted from HWYLA:
    // https://github.com/TehNut/HWYLA/blob/9d83ceb1d36733f11f9502378426626de246a7bf/src/main/java/mcp/mobius/waila/mixin/client/MixinGameRenderer.java#L14

    @Inject(method = {"render"},
            slice = {@Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V"))},
            at = {@At(value = "INVOKE", ordinal = 0)})
    private void renderOverlay(float partialTicks, long nanoTime, boolean var4, CallbackInfo callbackInfo) {
        StockpileClient.INSTANCE.drawOverlays(partialTicks);
    }
}