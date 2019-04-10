package me.branchpanic.mods.stockpile.api.storage

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import me.branchpanic.mods.stockpile.api.TestItems
import net.minecraft.item.ItemStack
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
class MassItemStorageSpec : WordSpec({
    "A mass ItemStack storage" should {
        "return an empty list when attempting to take from an empty storage" {
            val m = MassItemStorage(1)

            m.take(0) shouldBe emptyList()
            m.take(100) shouldBe emptyList()
            m.take(100_000_000) shouldBe emptyList()
        }

        "accept any stack when empty" {
            val m = MassItemStorage(1)

            m.accepts(ItemStack.EMPTY) shouldBe true
            m.accepts(ItemStack(TestItems.StandardItemA)) shouldBe true
            m.accepts(ItemStack(TestItems.StandardItemB)) shouldBe true
        }

        "accept only stacks of the same item and data when not empty" {
            val m = MassItemStorage(1, storedStack = ItemStack(TestItems.StandardItemA))

            m.accepts(ItemStack(TestItems.StandardItemA)) shouldBe true
            m.accepts(ItemStack(TestItems.StandardItemA, 5)) shouldBe true

            m.accepts(ItemStack.EMPTY) shouldBe false
            m.accepts(ItemStack(TestItems.StandardItemB)) shouldBe false
        }

        "change its instance if unset and offered a non-empty stack" {
            val m = MassItemStorage(1)

            m.instanceIsSet shouldBe false
            m.currentInstance shouldBe ItemStack.EMPTY

            m.offer(ItemStack(TestItems.StandardItemA))

            m.instanceIsSet shouldBe true
            m.currentInstance.item shouldBe TestItems.StandardItemA
        }

        "not change its instance when emptied and told to retain instance when empty" {
            val m = MassItemStorage(
                1,
                storedItems = 1L,
                storedStack = ItemStack(TestItems.StandardItemA)
            )

            m.retainInstanceWhenEmpty()
            m.remove(1L)

            m.instanceIsSet shouldBe true
            m.currentInstance.item shouldBe TestItems.StandardItemA
        }

        "change its instance when emptied and told to change instance when empty" {
            val m = MassItemStorage(
                1,
                storedItems = 1L,
                storedStack = ItemStack(TestItems.StandardItemA)
            )

            m.clearInstanceWhenEmpty()
            m.remove(1L)

            m.instanceIsSet shouldBe false
            m.currentInstance shouldBe ItemStack.EMPTY
        }

        "clear its instance when told to change instance when empty while retaining a previous instance" {
            val m = MassItemStorage(
                1,
                storedItems = 1L,
                storedStack = ItemStack(TestItems.StandardItemA)
            )

            m.retainInstanceWhenEmpty()
            m.remove(1L)
            m.clearInstanceWhenEmpty()

            m.instanceIsSet shouldBe false
            m.currentInstance shouldBe ItemStack.EMPTY
        }

        "accept one full stack when space is available for one" {
            val m = MassItemStorage(
                1,
                storedItems = 0L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            m.offer(ItemStack(TestItems.StandardItemA, 64)) shouldBe null
            m.amountStored shouldBe 64L
        }

        "accept multiple full stacks when space is available for them" {
            val m = MassItemStorage(
                3,
                storedItems = 0L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            m.offer(
                listOf(
                    ItemStack(TestItems.StandardItemA, 64),
                    ItemStack(TestItems.StandardItemA, 64),
                    ItemStack(TestItems.StandardItemA, 64)
                )
            ).isEmpty() shouldBe true

            m.amountStored shouldBe 64L * 3L
        }

        "accept part of a stack when space is available for it" {
            val m = MassItemStorage(
                1,
                storedItems = 32L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val remainder = m.offer(ItemStack(TestItems.StandardItemA, 64))

            remainder?.amount shouldBe 32
            remainder?.item shouldBe TestItems.StandardItemA
        }

        "accept part of a list of stacks when space is available for them" {
            val m = MassItemStorage(
                3,
                storedItems = 32L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val remainder = m.offer(
                listOf(
                    ItemStack(TestItems.StandardItemA, 64),
                    ItemStack(TestItems.StandardItemA, 64),
                    ItemStack(TestItems.StandardItemA, 64)
                )
            )

            remainder.size shouldBe 1
            remainder[0].amount shouldBe 32
            m.amountStored shouldBe 64L * 3L
        }

        "yield all of its remaining contents when the amount (1 stack) is specified exactly" {
            val m = MassItemStorage(
                1,
                storedItems = 64L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val taken = m.take(64)

            taken.size shouldBe 1
            taken[0].amount shouldBe 64
            m.amountStored shouldBe 0
        }

        "yield all of its remaining contents when the amount (>1 stack) is specified exactly" {
            val m = MassItemStorage(
                3,
                storedItems = 64L * 3L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val taken = m.take(64L * 3L)

            taken shouldHaveSize 3
            taken.forEach { s -> s.amount shouldBe 64 }
            m.amountStored shouldBe 0
        }

        "yield all of its remaining contents when an amount greater than that of its contents is specified" {
            val m = MassItemStorage(
                3,
                storedItems = 64L * 3L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val taken = m.take(64L * 10L)

            taken shouldHaveSize 3
            taken.forEach { s -> s.amount shouldBe 64 }
            m.amountStored shouldBe 0
        }

        "yield an empty list when empty" {
            val m = MassItemStorage(
                0,
                storedItems = 0L,
                storedStack = ItemStack(TestItems.StandardItemA),
                clearWhenEmpty = false
            )

            val taken = m.take(64L * 3L)

            taken shouldHaveSize 0
        }
    }
})