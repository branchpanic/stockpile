package me.branchpanic.mods.stockpile.impl.storage

// TODO(test): PowerMock appears to be broken (StackOverflow on test execution)

//@RunWith(PowerMockRunner::class)
//@PowerMockRunnerDelegate(KotlinTestRunner::class)
//@SuppressStaticInitializationFor("net.minecraft.item.Items", "net.minecraft.util.registry.Registry")
//@PrepareForTest(Items::class, Registry::class)
//class MassItemStackStorageSpec : StringSpec({
//    "addAtMost with Quantizer" {
//        forall(
//            row(ItemA.toQuantifier(64), 1, ItemA.toQuantifier(1), ItemA.toQuantifier(1)),
//            row(ItemA.toQuantifier(32), 1, ItemA.toQuantifier(64), ItemA.toQuantifier(32)),
//            row(ItemA.toQuantifier(32), 2, ItemA.toQuantifier(64), ItemA.toQuantifier(0)),
//            row(ItemA.toQuantifier(32), 1, ItemA.toQuantifier(1), ItemA.toQuantifier(0)),
//            row(ItemA.toQuantifier(32), 1, ItemA.toQuantifier(32), ItemA.toQuantifier(0)),
//            row(ItemA.toQuantifier(32), 1, ItemB.toQuantifier(1), ItemB.toQuantifier(1)),
//            row(ItemStackQuantifier.NONE, 1, ItemA.toQuantifier(32), ItemA.toQuantifier(0)),
//            row(ItemStackQuantifier.NONE, 1, ItemB.toQuantifier(32), ItemB.toQuantifier(0)),
//            row(ItemStackQuantifier.NONE, 1, ItemA.toQuantifier(128), ItemA.toQuantifier(64))
//        ) { contents, maxStacks, insertedQuantizer, expectedRemainder ->
//            booleanArrayOf(true, false).forEach { simulate ->
//                val storage = MassItemStackStorage(contents, maxStacks)
//                val remainder = storage.addAtMost(insertedQuantizer, simulate)
//
//                remainder shouldBe expectedRemainder
//
//                if (simulate || !contents.canMergeWith(insertedQuantizer)) {
//                    storage.contents shouldBe contents
//                } else {
//                    storage.contents shouldBe contents + insertedQuantizer - expectedRemainder
//                }
//            }
//        }
//    }
//
//    "addAtMost with Long" {
//        forall(
//            row(64L, 1, 1L, 1L),
//            row(0L, 1, 1L, 0L),
//            row(0L, 1, 128L, 64L),
//            row(0L, 2, 128L, 0L)
//        ) { contentsAmount, maxStacks, insertedAmount, remainderAmount ->
//            booleanArrayOf(true, false).forEach { simulate ->
//                val storage = MassItemStackStorage(ItemA.toQuantifier(contentsAmount), maxStacks)
//                val remainder = storage.addAtMost(insertedAmount, simulate)
//
//                remainder shouldBe remainderAmount
//
//                if (simulate) {
//                    storage.contents.amount shouldBe contentsAmount
//                } else {
//                    storage.contents.amount shouldBe contentsAmount + insertedAmount - remainderAmount
//                }
//            }
//        }
//    }
//})
