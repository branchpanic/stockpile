package notjoe.stockpile.testutil

import io.kotlintest.Matcher
import io.kotlintest.Result
import net.minecraft.item.ItemStack
import notjoe.stockpile.util.ext.isStackableWith

fun matchStack(stack: ItemStack) = object : Matcher<ItemStack> {
    override fun test(value: ItemStack): Result = Result(value.isStackableWith(stack), "given ItemStack $value is not stackable with $stack", "$value should not be stackable with $stack")
}