package notjoe.stockpile.storage.inventory

import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import notjoe.stockpile.testutil.*
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

    val inventory = MassItemInventory(ItemStack.EMPTY, 32)

    "All inventories" should {
        "accept any item when first initialized" {
            inventory.insertStack(1 of RED_ITEM) shouldBe ItemStack.EMPTY
            inventory.isEmpty shouldBe false
        }

        "have their stack type set when the first item is inserted" {
            inventory.insertStack(1 of RED_ITEM)
            inventory.typeIsDefined shouldBe true
            inventory.stackType should matchStack(1 of RED_ITEM)
        }

        "reject items that don't match the stored stack type" {
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
    }

    "Unlocked inventories" should {
        inventory.typeIsLocked = false

        "accept new item types upon being emptied" {
            inventory.insertStack(1 of RED_ITEM)
            inventory.removeStackFromSlot(0)

            inventory.insertStack(1 of BLUE_ITEM) shouldBe ItemStack.EMPTY
            inventory.isEmpty shouldBe false
        }
    }

    "Locked inventories" should {
        inventory.typeIsLocked = true

        "not accept new item types upon being emptied" {
            inventory.insertStack(1 of RED_ITEM)
            inventory.removeStackFromSlot(0)

            val blueItemStack = 1 of BLUE_ITEM
            inventory.insertStack(blueItemStack) should matchStack(blueItemStack)
            inventory.isEmpty shouldBe true
        }
    }
})
