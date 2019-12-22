package me.branchpanic.mods.stockpile.impl.storage

// TODO(test): PowerMock appears to be broken (StackOverflow on test execution)

//@RunWith(PowerMockRunner::class)
//@PowerMockRunnerDelegate(KotlinTestRunner::class)
//@SuppressStaticInitializationFor("net.minecraft.item.Items", "net.minecraft.util.registry.Registry")
//@PrepareForTest(Items::class, Registry::class)
//class ItemStackQuantifierSpec : StringSpec({
//    "plus" {
//        forall(
//            row(ItemA.toQuantifier(1), ItemA.toQuantifier(1), ItemA.toQuantifier(2)),
//            row(ItemStackQuantifier.NONE, ItemA.toQuantifier(1), ItemA.toQuantifier(1))
//        ) { quantizerA, quantizerB, sum ->
//            quantizerA + quantizerB shouldBe sum
//        }
//    }
//
//    "canMergeWith" {
//        forall(
//            row(ItemA.toQuantifier(1), ItemA.toQuantifier(1), true),
//            row(ItemA.toQuantifier(5), ItemA.toQuantifier(1), true),
//            row(ItemA.toQuantifier(1), ItemA.toQuantifier(5), true),
//            row(ItemA.toQuantifier(1), ItemStackQuantifier.NONE, true),
//            row(ItemStackQuantifier.NONE, ItemA.toQuantifier(1), true),
//            row(ItemA.toQuantifier(1), (1 of ItemA).withDummyTag().toQuantifier(), false),
//            row(ItemA.toQuantifier(1), ItemB.toQuantifier(1), false)
//        ) { quantizerA, quantizerB, canMerge ->
//            quantizerA.canMergeWith(quantizerB) shouldBe canMerge
//        }
//    }
//
//    "toObjects" {
//        forall(
//            row(ItemA.toQuantifier(1), listOf(1 of ItemA)),
//            row(ItemA.toQuantifier(5), listOf(5 of ItemA)),
//            row(ItemA.toQuantifier(0), emptyList()),
//            row(ItemA.toQuantifier(64), listOf(64 of ItemA)),
//            row(ItemA.toQuantifier(65), listOf(64 of ItemA, 1 of ItemA)),
//            row(ItemA.toQuantifier(128), listOf(64 of ItemA, 64 of ItemA)),
//            row(ItemA.toQuantifier(129), listOf(64 of ItemA, 64 of ItemA, 1 of ItemA))
//        ) { quantizer, expectedObjects ->
//            quantizer.toObjects().map { s -> s.item to s.count } shouldContainExactlyInAnyOrder expectedObjects.map { s -> s.item to s.count }
//        }
//    }
//
//    "equals" {
//        forall(
//            row(ItemA.toQuantifier(1), ItemA.toQuantifier(1), true),
//            row(ItemA.toQuantifier(1), ItemA.toQuantifier(5), false),
//            row(ItemA.toQuantifier(0), ItemA.toQuantifier(1), false),
//            row(ItemStackQuantifier.NONE, ItemStackQuantifier.NONE, true),
//            row(ItemA.toQuantifier(1), ItemB.toQuantifier(1), false),
//            row(ItemA.toQuantifier(1), ItemB.toQuantifier(0), false),
//            row(ItemA.toQuantifier(1), ItemStackQuantifier.NONE, false),
//            row((1 of ItemA).withDummyTag().toQuantifier(1), ItemA.toQuantifier(1), false)
//        ) { quantizerA, quantizerB, shouldEqual ->
//            (quantizerA == quantizerB) shouldBe shouldEqual
//        }
//    }
//})