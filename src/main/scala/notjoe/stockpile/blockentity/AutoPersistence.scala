package notjoe.stockpile.blockentity

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import notjoe.cereal.deserialization.InPlaceDeserializer
import notjoe.cereal.serialization.{CompoundTagSerializer, Persistent}

import scala.annotation.meta.field

object AutoPersistence {
  type PersistentField = Persistent@field
}

trait AutoPersistence extends BlockEntity {
  val serializer: CompoundTagSerializer = CompoundTagSerializer.withDefaults()
  val deserializer: InPlaceDeserializer = InPlaceDeserializer.forObject(this)

  def persistentDataToTag(): CompoundTag = serializer.serializeToCompound(this)

  def loadPersistentDataFromTag(tag: CompoundTag): Unit = {
    deserializer.deserializeFromTag(tag, false)
  }

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

