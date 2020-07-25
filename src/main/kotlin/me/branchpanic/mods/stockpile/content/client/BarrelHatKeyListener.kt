package me.branchpanic.mods.stockpile.content.client

import io.netty.buffer.Unpooled
import me.branchpanic.mods.stockpile.Stockpile
import me.branchpanic.mods.stockpile.StockpileClient
import me.branchpanic.mods.stockpile.content.item.BarrelHatItem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.event.client.ClientTickCallback
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.PacketByteBuf

@Environment(EnvType.CLIENT)
object BarrelHatKeyListener : ClientTickCallback {
    private var keyWasPressed = false

    override fun tick(client: MinecraftClient?) {
        if (StockpileClient.BARREL_HAT_KEY.isPressed) {
            if (keyWasPressed) {
                return
            } else {
                keyWasPressed = true
            }
        } else {
            keyWasPressed = false
            return
        }

        if (client?.player?.getEquippedStack(EquipmentSlot.HEAD)?.item != BarrelHatItem) {
            return
        }

        ClientSidePacketRegistry.INSTANCE.sendToServer(
            Stockpile.id("barrel_hat_restock"),
            PacketByteBuf(Unpooled.buffer())
        )
    }
}