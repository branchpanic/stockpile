package notjoe.stockpile.blockentity

import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import notjoe.cereal.deserialization.CompoundTagDeserializer
import notjoe.cereal.serialization.{CompoundTagSerializer, Persistent}

import scala.annotation.meta.field

trait AutoPersistence extends BlockEntity {
  final val SERIALIZER = CompoundTagSerializer.withDefaults()
  final val DESERIALIZER = CompoundTagDeserializer.withDefaults()

  def persistentDataToTag(): CompoundTag = SERIALIZER.serializeToCompound(this)

  def loadPersistentDataFromTag(tag: CompoundTag): Unit = {
    if (tag.containsKey("PersistentData")) {
      DESERIALIZER.deserializeInPlace(this, tag.getCompound("PersistentData"))
    }
  }

  abstract override def toTag(tag: CompoundTag): CompoundTag = {
    val internalTag = super.toTag(tag)
    internalTag.put("PersistentData", persistentDataToTag())
    internalTag
  }

  abstract override def fromTag(tag: CompoundTag): Unit = {
    super.fromTag(tag)
    loadPersistentDataFromTag(tag)
  }
}

object AutoPersistence {
  type PersistentField = Persistent@field
}
