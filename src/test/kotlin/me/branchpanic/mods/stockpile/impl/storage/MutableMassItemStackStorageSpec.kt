package me.branchpanic.mods.stockpile.impl.storage

import io.kotlintest.runner.junit4.KotlinTestRunner
import io.kotlintest.specs.WordSpec
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
class MutableMassItemStackStorageSpec : WordSpec({

})