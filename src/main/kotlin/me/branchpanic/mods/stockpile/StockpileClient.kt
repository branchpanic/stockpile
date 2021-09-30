package me.branchpanic.mods.stockpile

import me.branchpanic.mods.stockpile.content.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.content.client.BarrelHatKeyListener
import me.branchpanic.mods.stockpile.content.client.gui.OverlayRenderer
import me.branchpanic.mods.stockpile.content.client.gui.UpgradeOverlayRenderer
import me.branchpanic.mods.stockpile.content.client.gui.UpgradeRemoverOverlayRenderer
import me.branchpanic.mods.stockpile.content.client.renderer.ItemBarrelRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.event.client.ClientTickCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import org.lwjgl.glfw.GLFW

@Environment(EnvType.CLIENT)
object StockpileClient : ClientModInitializer {
    val BARREL_HAT_KEY: FabricKeyBinding = FabricKeyBinding.Builder.create(
        Stockpile.id("barrel_hat"),
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        "controls.stockpile"
    ).build()

    private val overlays: List<OverlayRenderer> = listOf(
        UpgradeOverlayRenderer(),
        UpgradeRemoverOverlayRenderer()
    )

    override fun onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(ItemBarrelBlockEntity.TYPE, { ItemBarrelRenderer() } )

        KeyBindingRegistry.INSTANCE.addCategory("controls.stockpile")
        KeyBindingRegistry.INSTANCE.register(BARREL_HAT_KEY)

        ClientTickCallback.EVENT.register(BarrelHatKeyListener)
    }

    fun drawOverlays(partialTicks: Float) {

        // Adapted from HWYLA:
        // https://github.com/TehNut/HWYLA/blob/9d83ceb1d36733f11f9502378426626de246a7bf/src/main/java/mcp/mobius/waila/overlay/RayTracing.java#L62

        val mc = MinecraftClient.getInstance()

        val interactionManager = mc.interactionManager ?: return
        val player = mc.player ?: return
        val world = mc.world ?: return

        val playerReach = interactionManager.reachDistance
        val eyePosition = player.getCameraPosVec(partialTicks)
        val lookVector = player.getRotationVec(partialTicks)
        val traceEnd =
            eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach)

        val context = RaycastContext(
            eyePosition,
            traceEnd,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        )

        val result = world.raycast(context)

        if (result.type != HitResult.Type.BLOCK) {
            return
        }

        overlays.forEach { o ->
            o.draw(world, player.mainHandStack, result.blockPos)
        }
    }
}
