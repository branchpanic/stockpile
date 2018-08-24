package notjoe.stockpile.testutil

import net.minecraft.init.Items
import org.powermock.reflect.Whitebox

/**
 * Stubs out definitions in Minecraft's Items class with generic items.
 *
 * @param items Items to stub, represented as their field names in net.minecraft.init.Items (i.e. "AIR")
 */
fun stubItems(vararg items: String) {
    items.forEach { Whitebox.setInternalState(Items::class.java, it, TestItem(it)) }
}