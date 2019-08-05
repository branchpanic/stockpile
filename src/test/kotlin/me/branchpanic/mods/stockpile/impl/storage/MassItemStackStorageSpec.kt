package me.branchpanic.mods.stockpile.impl.storage

import io.kotlintest.data.forall
import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import me.branchpanic.mods.stockpile.TestItems.ItemA
import me.branchpanic.mods.stockpile.TestItems.ItemB
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
class MassItemStackStorageSpec : StringSpec({
    "addAtMost with Quantizer" {
        forall(
            row(ItemA.toQuantizer(64), 1, ItemA.toQuantizer(1), ItemA.toQuantizer(1)),
            row(ItemA.toQuantizer(32), 1, ItemA.toQuantizer(64), ItemA.toQuantizer(32)),
            row(ItemA.toQuantizer(32), 2, ItemA.toQuantizer(64), ItemStackQuantizer.NONE),
            row(ItemA.toQuantizer(32), 1, ItemA.toQuantizer(1), ItemStackQuantizer.NONE),
            row(ItemA.toQuantizer(32), 1, ItemA.toQuantizer(32), ItemStackQuantizer.NONE),
            row(ItemA.toQuantizer(32), 1, ItemB.toQuantizer(1), ItemB.toQuantizer(1)),
            row(ItemStackQuantizer.NONE, 1, ItemA.toQuantizer(32), ItemStackQuantizer.NONE),
            row(ItemStackQuantizer.NONE, 1, ItemB.toQuantizer(32), ItemStackQuantizer.NONE),
            row(ItemStackQuantizer.NONE, 1, ItemA.toQuantizer(128), ItemA.toQuantizer(64))
        ) { contents, maxStacks, insertedQuantizer, expectedRemainder ->
            booleanArrayOf(true, false).forEach { simulate ->
                val storage = MassItemStackStorage(contents, maxStacks)
                val remainder = storage.addAtMost(insertedQuantizer, simulate)

                remainder shouldBe expectedRemainder

                if (simulate || !contents.canMergeWith(insertedQuantizer)) {
                    storage.contents shouldBe contents
                } else {
                    storage.contents shouldBe contents + insertedQuantizer - expectedRemainder
                }
            }
        }
    }

    "addAtMost with Long" {
        forall(
            row(64L, 1, 1L, 1L),
            row(0L, 1, 1L, 0L),
            row(0L, 1, 128L, 64L),
            row(0L, 2, 128L, 0L)
        ) { contentsAmount, maxStacks, insertedAmount, remainderAmount ->
            booleanArrayOf(true, false).forEach { simulate ->
                val storage = MassItemStackStorage(ItemA.toQuantizer(contentsAmount), maxStacks)
                val remainder = storage.addAtMost(insertedAmount, simulate)

                remainder shouldBe remainderAmount

                if (simulate) {
                    storage.contents.amount shouldBe contentsAmount
                } else {
                    storage.contents.amount shouldBe contentsAmount + insertedAmount - remainderAmount
                }
            }
        }
    }

    "removeAtMost with Quantizer" {
        // TODO(test)
    }

    "removeAtMost with Long" {
        // TODO(test)
    }
})
