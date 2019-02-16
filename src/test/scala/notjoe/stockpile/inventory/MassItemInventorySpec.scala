package notjoe.stockpile.inventory

import net.minecraft.item.{ItemStack, Items}
import net.minecraft.util.registry.Registry
import notjoe.stockpile.test.TestItems
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.{PrepareForTest, SuppressStaticInitializationFor}
import org.powermock.modules.junit4.{PowerMockRunner, PowerMockRunnerDelegate}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[PowerMockRunner])
@PowerMockRunnerDelegate(classOf[JUnitRunner])
@SuppressStaticInitializationFor(Array("net.minecraft.item.Items", "net.minecraft.util.registry.Registry"))
@PrepareForTest(Array(classOf[Items], classOf[Registry[_]]))
class MassItemInventorySpec extends FlatSpec with Matchers {
  "A MassItemInventory" should "accept any item when first created" in {
    val inventory = new MassItemInventory()
    inventory.isAcceptingNewStackType shouldBe true
  }

  it should "be empty when first created" in {
    val inventory = new MassItemInventory()
    inventory.isInvEmpty shouldBe true
  }

  it should "update its stored stack type when the first item is inserted" in {
    val inventory = new MassItemInventory()
    val insertedStack = new ItemStack(TestItems.Red, 1)

    inventory.insertStack(insertedStack)

    ItemStack.areEqual(inventory.stackType, insertedStack) shouldBe true
  }

  it should "not be empty after the first item is inserted" in {
    val inventory = new MassItemInventory()
    val insertedStack = new ItemStack(TestItems.Red, 1)

    inventory.insertStack(insertedStack)

    inventory.isInvEmpty shouldBe false
  }

  it should "not accept an item of a different type than what's stored inside" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1)

    val remainder = inventory.insertStack(new ItemStack(TestItems.Blue, 1))

    ItemStack.areEqual(remainder, new ItemStack(TestItems.Blue, 1)) shouldBe true
  }

  it should "not accept items past its capacity" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), maxStacks = 2, amountStored = 64 + 63)

    val remainder = inventory.insertStack(new ItemStack(TestItems.Red, 2))

    ItemStack.areEqual(remainder, new ItemStack(TestItems.Red, 1)) shouldBe true
  }

  it should "allow a new item type when emptied and set to accept new stacks" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1, allowNewStackWhenEmpty = true)

    inventory.removeInvStack(MassItemInventory.OutputSlotIndex)

    val remainder = inventory.insertStack(new ItemStack(TestItems.Blue, 1))

    ItemStack.areEqual(inventory.stackType, new ItemStack(TestItems.Blue, 1)) shouldBe true
    remainder.isEmpty shouldBe true
  }

  it should "not allow a new item type when emptied and not set to accept new stacks" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1, allowNewStackWhenEmpty = false)

    inventory.removeInvStack(MassItemInventory.OutputSlotIndex)

    val remainder = inventory.insertStack(new ItemStack(TestItems.Blue, 1))

    ItemStack.areEqual(remainder, new ItemStack(TestItems.Blue, 1)) shouldBe true
    ItemStack.areEqual(inventory.stackType, new ItemStack(TestItems.Red, 1)) shouldBe true
  }

  it should "never report items as valid for its output slot" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1)
    inventory.isValidInvStack(MassItemInventory.OutputSlotIndex, new ItemStack(TestItems.Red)) shouldBe false
    inventory.isValidInvStack(MassItemInventory.OutputSlotIndex, new ItemStack(TestItems.Blue)) shouldBe false
  }

  it should "report only stacks with the same item type as valid" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1)
    inventory.isValidInvStack(MassItemInventory.InputSlotIndex, new ItemStack(TestItems.Red)) shouldBe true
    inventory.isValidInvStack(MassItemInventory.InputSlotIndex, new ItemStack(TestItems.Blue)) shouldBe false
  }

  it should "only start to fill its input slot once it's within the last stack of its capacity" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 64, maxStacks = 2)
    inventory.getInvStack(MassItemInventory.InputSlotIndex).isEmpty shouldBe true

    inventory.insertStack(new ItemStack(TestItems.Red, 3))

    ItemStack.areEqual(inventory.getInvStack(MassItemInventory.InputSlotIndex), new ItemStack(TestItems.Red, 3)) shouldBe true
  }

  it should "lower its stored amount when stacks are taken" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 128)

    inventory.removeInvStack(MassItemInventory.OutputSlotIndex)

    inventory.amountStored shouldBe 64
  }

  it should "set its stored amount to 0 and stack type to empty when cleared" in {
    val inventory = new MassItemInventory(_stackType =  new ItemStack(TestItems.Red), amountStored = 1)
    inventory.method_5448()

    inventory.amountStored shouldBe 0
    inventory.stackType.isEmpty shouldBe true
  }
}
