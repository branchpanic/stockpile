@file:Suppress("INACCESSIBLE_TYPE")

package me.branchpanic.mods.stockpile.client

import io.netty.buffer.Unpooled
import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.blockentity.ItemBarrelBlockEntity
import me.branchpanic.mods.stockpile.blockentity.ItemStorageDeviceBlockEntity
import me.branchpanic.mods.stockpile.client.gui.UpgradeOverlayRenderer
import me.branchpanic.mods.stockpile.client.gui.UpgradeRemoverOverlayRenderer
import me.branchpanic.mods.stockpile.client.renderer.ItemBarrelRenderer
import me.branchpanic.mods.stockpile.client.renderer.ItemStorageDeviceRenderer
import me.branchpanic.mods.stockpile.item.BarrelHatItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

@Environment(EnvType.CLIENT)
object StockpileClient : ClientModInitializer {
    val barrelHatBinding =
        KeyBinding(Stockpile.id("barrel_hat").toString(), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "controls.stockpile")

    private val overlays = listOf(
        UpgradeOverlayRenderer(),
        UpgradeRemoverOverlayRenderer()
    )

    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(barrelHatBinding)
        BlockEntityRendererRegistry.register(ItemBarrelBlockEntity.TYPE) { ItemBarrelRenderer() }
        BlockEntityRendererRegistry.register(ItemStorageDeviceBlockEntity.TYPE) { ctx ->
            ItemStorageDeviceRenderer(ctx)
        }

        ClientTickEvents.START_CLIENT_TICK.register { client ->
            if (!barrelHatBinding.wasPressed()) return@register
            if (client?.player?.getEquippedStack(EquipmentSlot.HEAD)?.item != BarrelHatItem) return@register

            ClientPlayNetworking.send(Stockpile.id("barrel_hat_restock"), PacketByteBuf(Unpooled.buffer()))
        }
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
