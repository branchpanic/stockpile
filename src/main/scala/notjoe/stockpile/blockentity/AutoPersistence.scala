package notjoe.stockpile.blockentity

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import notjoe.cereal.TagStorage
import notjoe.cereal.persistence.Persistent
import scala.annotation.meta.field

object AutoPersistence {
  type PersistentField = Persistent@field
}

trait AutoPersistence extends BlockEntity {
  def persistentDataToTag(): CompoundTag = TagStorage.toTag(this)

  def loadPersistentDataFromTag(tag: CompoundTag): Unit = TagStorage.loadState(this, tag)

  abstract override def toTag(tag: CompoundTag): CompoundTag = {
    val internalTag = super.toTag(tag)
    internalTag.put("PersistentData", persistentDataToTag())
    internalTag
  }

  abstract override def fromTag(tag: CompoundTag): Unit = {
    super.fromTag(tag)
    if (tag.containsKey("PersistentData", NbtType.COMPOUND)) {
      loadPersistentDataFromTag(tag.getCompound("PersistentData"))
    }
  }
}

