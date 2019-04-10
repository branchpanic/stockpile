package me.branchpanic.mods.stockpile.api.inventory

import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import me.branchpanic.mods.stockpile.api.TestItems
import me.branchpanic.mods.stockpile.api.storage.MassItemStorage
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
class MassItemInventorySpec : WordSpec({
    "A mass ItemStack Inventory" should {
        "only accept the item contained in the associated MassStorage" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 0L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            )

            inv.isValidInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA)) shouldBe true
            inv.canInsertInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA), null) shouldBe true

            inv.isValidInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemB)) shouldBe false
            inv.canInsertInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemB), null) shouldBe false
        }

        "only accept items in its designated input slot" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 0L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            )

            inv.isValidInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA)) shouldBe true
            inv.canInsertInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA), null) shouldBe true

            inv.isValidInvStack(MassItemInventory.OUTPUT_SLOT, ItemStack(TestItems.StandardItemA)) shouldBe false
            inv.canInsertInvStack(MassItemInventory.OUTPUT_SLOT, ItemStack(TestItems.StandardItemA), null) shouldBe false
        }

        "update the MassStorage's stored item through insertion" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 0L,
                    storedStack = ItemStack.EMPTY,
                    clearWhenEmpty = true
                )
            )

            inv.storage.instanceIsSet shouldBe false

            inv.setInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA, 1))

            inv.storage.instanceIsSet shouldBe true
            inv.storage.currentInstance.item shouldBe TestItems.StandardItemA
            inv.storage.amountStored shouldBe 1L
        }

        "update the MassStorage's stored item through extraction" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 64L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            )

            inv.storage.instanceIsSet shouldBe true

            inv.takeInvStack(MassItemInventory.OUTPUT_SLOT, 64)


            inv.storage.instanceIsSet shouldBe false
            inv.storage.currentInstance.isEmpty shouldBe true
            inv.storage.amountStored shouldBe 0L
        }

        "yield one stack's worth of contents in the output slot" {
            MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 32L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.OUTPUT_SLOT).amount shouldBe 32

            MassItemInventory(
                MassItemStorage(
                    maxStacks = 2,
                    storedItems = 128L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.OUTPUT_SLOT).amount shouldBe 64

            MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 0L,
                    storedStack = ItemStack.EMPTY,
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.OUTPUT_SLOT).amount shouldBe 0
        }

        "start filling the input slot only when within one stack of max capacity" {
            MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 32L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.INPUT_SLOT).amount shouldBe 0

            MassItemInventory(
                MassItemStorage(
                    maxStacks = 2,
                    storedItems = 128L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.INPUT_SLOT).amount shouldBe 64

            MassItemInventory(
                MassItemStorage(
                    maxStacks = 2,
                    storedItems = 120L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = true
                )
            ).getInvStack(MassItemInventory.INPUT_SLOT).amount shouldBe 56
        }

        "remove items from the MassStorage through takeInvStack" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 2,
                    storedItems = 128L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = false
                )
            )

            inv.takeInvStack(MassItemInventory.OUTPUT_SLOT, 64).apply {
                item shouldBe TestItems.StandardItemA
                amount shouldBe 64
            }

            inv.storage.amountStored shouldBe 64L

            inv.takeInvStack(MassItemInventory.OUTPUT_SLOT, 32).apply {
                item shouldBe TestItems.StandardItemA
                amount shouldBe 32
            }

            inv.storage.amountStored shouldBe 32L

            inv.takeInvStack(MassItemInventory.OUTPUT_SLOT, 64).apply {
                item shouldBe TestItems.StandardItemA
                amount shouldBe 32
            }

            inv.storage.amountStored shouldBe 0L
        }

        "remove items from the MassStorage through remove" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 2,
                    storedItems = 128L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = false
                )
            )

            inv.removeInvStack(MassItemInventory.OUTPUT_SLOT).apply {
                item shouldBe TestItems.StandardItemA
                amount shouldBe 64
            }

            inv.storage.amountStored shouldBe 64L

            inv.removeInvStack(MassItemInventory.OUTPUT_SLOT).apply {
                item shouldBe TestItems.StandardItemA
                amount shouldBe 64
            }

            inv.storage.amountStored shouldBe 0L
        }

        "add items to the MassStorage dangerously via setInvStack" {
            val inv = MassItemInventory(
                MassItemStorage(
                    maxStacks = 1,
                    storedItems = 0L,
                    storedStack = ItemStack(TestItems.StandardItemA),
                    clearWhenEmpty = false
                )
            )

            inv.setInvStack(MassItemInventory.INPUT_SLOT, ItemStack(TestItems.StandardItemA, 64))

            inv.storage.amountStored shouldBe 64L
        }
    }
})