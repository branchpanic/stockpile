package me.branchpanic.mods.stockpile.impl.storage

import io.kotlintest.data.forall
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import me.branchpanic.mods.stockpile.TestItems.ItemA
import me.branchpanic.mods.stockpile.TestItems.ItemB
import me.branchpanic.mods.stockpile.of
import me.branchpanic.mods.stockpile.withDummyTag
import net.minecraft.item.Items
import net.minecraft.util.registry.Registry
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(KotlinTestRunner::class)
@SuppressStaticInitializationFor("net.minecraft.item.Items", "net.minecraft.util.registry.Registry")
@PrepareForTest(Items::class, Registry::class)
class ItemStackQuantizerSpec : StringSpec({
    "canMergeWith" {
        forall(
            row(ItemA.toQuantizer(1), ItemA.toQuantizer(1), true),
            row(ItemA.toQuantizer(5), ItemA.toQuantizer(1), true),
            row(ItemA.toQuantizer(1), ItemA.toQuantizer(5), true),
            row(ItemA.toQuantizer(1), ItemStackQuantizer.NONE, true),
            row(ItemStackQuantizer.NONE, ItemA.toQuantizer(1), true),
            row(ItemA.toQuantizer(1), (1 of ItemA).withDummyTag().toQuantizer(), false),
            row(ItemA.toQuantizer(1), ItemB.toQuantizer(1), false)
        ) { quantizerA, quantizerB, canMerge ->
            quantizerA.canMergeWith(quantizerB) shouldBe canMerge
        }
    }

    "toObjects" {
        forall(
            row(ItemA.toQuantizer(1), listOf(1 of ItemA)),
            row(ItemA.toQuantizer(5), listOf(5 of ItemA)),
            row(ItemA.toQuantizer(0), emptyList()),
            row(ItemA.toQuantizer(64), listOf(64 of ItemA)),
            row(ItemA.toQuantizer(65), listOf(64 of ItemA, 1 of ItemA)),
            row(ItemA.toQuantizer(128), listOf(64 of ItemA, 64 of ItemA)),
            row(ItemA.toQuantizer(129), listOf(64 of ItemA, 64 of ItemA, 1 of ItemA))
        ) { quantizer, expectedObjects ->
            quantizer.toObjects().map { s -> s.item to s.count } shouldContainExactlyInAnyOrder expectedObjects.map { s -> s.item to s.count }
        }
    }

    "equals" {
        forall(
            row(ItemA.toQuantizer(1), ItemA.toQuantizer(1), true),
            row(ItemA.toQuantizer(1), ItemA.toQuantizer(5), false),
            row(ItemA.toQuantizer(0), ItemA.toQuantizer(1), false),
            row(ItemStackQuantizer.NONE, ItemStackQuantizer.NONE, true),
            row(ItemA.toQuantizer(1), ItemB.toQuantizer(1), false),
            row(ItemA.toQuantizer(1), ItemB.toQuantizer(0), false),
            row(ItemA.toQuantizer(1), ItemStackQuantizer.NONE, false),
            row((1 of ItemA).withDummyTag().toQuantizer(1), ItemA.toQuantizer(1), false)
        ) { quantizerA, quantizerB, shouldEqual ->
            (quantizerA == quantizerB) shouldBe shouldEqual
        }
    }
})