package notjoe.stockpile.storage.inventory

import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import notjoe.stockpile.testutil.BLUE_ITEM
import notjoe.stockpile.testutil.RED_ITEM
import notjoe.stockpile.testutil.matchStack
import notjoe.stockpile.testutil.of
import notjoe.stockpile.testutil.stubItems
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(KotlinTestRunner::class)
@PowerMockIgnore("javax.management.*")
@SuppressStaticInitializationFor("net.minecraft.init.Items")
@PrepareForTest(Items::class)
class MassItemInventorySpec : WordSpec({
    stubItems("AIR")

    "All inventories" should {

        "accept any item when first initialized" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.insertStack(1 of RED_ITEM) shouldBe ItemStack.EMPTY
            inventory.isEmpty shouldBe false
            inventory.amount shouldBe 1
        }

        "have their stack type set when the first item is inserted" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.insertStack(1 of BLUE_ITEM)
            inventory.typeIsDefined shouldBe true
            inventory.stackType should matchStack(1 of BLUE_ITEM)
        }

        "reject items that don't match the stored stack type" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.insertStack(1 of RED_ITEM)

            val blueItemStack = 1 of BLUE_ITEM
            inventory.insertStack(blueItemStack) should matchStack(blueItemStack)
        }

        "reject items that are inserted past the stored stack limit" {
            val tinyInventory = MassItemInventory(ItemStack.EMPTY, 1)
            tinyInventory.insertStack(64 of RED_ITEM)

            val redItemStack = 1 of RED_ITEM
            tinyInventory.insertStack(redItemStack) should matchStack(redItemStack)
        }

        "provide as many items as possible in their backing output slots" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.insertStack(1 of RED_ITEM)
            inventory.getStackInSlot(MASS_INVENTORY_OUTPUT_SLOT).count shouldBe 1
        }

        "provide items in their input slots within a stack of being full" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 2)
            inventory.insertStack(64 of RED_ITEM)
            inventory.getStackInSlot(MASS_INVENTORY_INPUT_SLOT).count shouldBe 0

            inventory.insertStack(1 of RED_ITEM)
            inventory.getStackInSlot(MASS_INVENTORY_INPUT_SLOT).count shouldBe 1
        }
    }

    "Unlocked inventories" should {
        "accept new item types upon being emptied" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = false

            inventory.insertStack(1 of RED_ITEM)
            inventory.removeStackFromSlot(0)

            inventory.insertStack(1 of BLUE_ITEM) shouldBe ItemStack.EMPTY
            inventory.isEmpty shouldBe false
        }

        "not report a defined type when empty" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = false

            inventory.insertStack(1 of RED_ITEM)
            inventory.typeIsDefined shouldBe true

            inventory.removeStackFromSlot(0)
            inventory.typeIsDefined shouldBe false
        }
    }

    "Locked inventories" should {

        "not accept new item types upon being emptied" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = true

            inventory.insertStack(1 of RED_ITEM)
            inventory.removeStackFromSlot(0)

            val blueItemStack = 1 of BLUE_ITEM
            inventory.insertStack(blueItemStack) should matchStack(blueItemStack)
            inventory.isEmpty shouldBe true
        }

        "start accepting new items after unlocked when empty" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = true

            inventory.insertStack(1 of RED_ITEM)
            inventory.removeStackFromSlot(0)

            inventory.typeIsLocked = false

            val blueItemStack = 1 of BLUE_ITEM
            inventory.insertStack(blueItemStack) shouldBe ItemStack.EMPTY
            inventory.isEmpty shouldBe false
        }

        "continue reporting a defined type when emptied" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = true

            inventory.typeIsDefined shouldBe false

            inventory.insertStack(1 of RED_ITEM)
            inventory.typeIsDefined shouldBe true

            inventory.removeStackFromSlot(0)
            inventory.typeIsDefined shouldBe true
        }

        "stop reporting a defined type when empty and unlocked" {
            val inventory = MassItemInventory(ItemStack.EMPTY, 32)
            inventory.typeIsLocked = true

            inventory.typeIsDefined shouldBe false

            inventory.insertStack(1 of RED_ITEM)
            inventory.typeIsDefined shouldBe true

            inventory.removeStackFromSlot(0)
            inventory.typeIsDefined shouldBe true

            inventory.typeIsLocked = false
            inventory.typeIsDefined shouldBe false
        }
    }
})
