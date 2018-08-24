package notjoe.stockpile.testutil

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IItemProvider

class TestItem(val name: String, builder: Builder? = Item.Builder()) : Item(builder) {
    override fun makeUnlocalizedName(): String {
        return "test.$name"
    }

    override fun toString(): String {
        return "[TEST] $name"
    }
}

val RED_ITEM: Item = TestItem("red")
val BLUE_ITEM: Item = TestItem("blue")

infix fun Int.of(item: IItemProvider) = ItemStack(item, this)