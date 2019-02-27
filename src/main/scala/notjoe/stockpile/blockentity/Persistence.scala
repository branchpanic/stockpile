package notjoe.stockpile.blockentity

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag

trait Persistence {
  def saveToTag(): CompoundTag
  def loadFromTag(tag: CompoundTag): Unit
}

trait BlockEntityPersistence extends BlockEntity with Persistence {

  // Hello! This design choice may seem a bit questionable, so here's some context as to why this exists.
  // Fabric-based Stockpile versions <= 0.4.6 depended on a serialization library I made, Cereal, that took care of
  // serializing fields automatically and bundled them up into a single compound. Looking at tags for these earlier
  // versions might shed some light on how this was used.
  //
  // I've since removed Cereal as a number of reasons, but to maintain compatibility, this PersistentData pattern
  // is needed. It will be removed following a breaking change.
  //
  // I've also learned my lesson about trying to make things simpler than they should be...

  abstract override def toTag(tag: CompoundTag): CompoundTag = {
    val internalTag = super.toTag(tag)
    internalTag.put("PersistentData", saveToTag())
    internalTag
  }

  abstract override def fromTag(tag: CompoundTag): Unit = {
    super.fromTag(tag)
    if (tag.containsKey("PersistentData", NbtType.COMPOUND)) {
      loadFromTag(tag.getCompound("PersistentData"))
    }
  }
}
