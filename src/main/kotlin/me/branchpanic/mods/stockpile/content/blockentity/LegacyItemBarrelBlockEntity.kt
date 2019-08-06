package me.branchpanic.mods.stockpile.content.blockentity

@Deprecated("No longer used", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("ItemBarrelBlockEntity"))
class LegacyItemBarrelBlockEntity {

    companion object {
        const val DEFAULT_CAPACITY_STACKS = 32
        const val MAX_UPGRADES = 6

        const val STORED_ITEM_TAG = "StoredItem"
        const val AMOUNT_STORED_TAG = "AmountStored"
        const val CLEAR_WHEN_EMPTY_TAG = "ClearWhenEmpty"
        const val UPGRADE_TAG = "Upgrades"

        const val RIGHT_CLICK_PERIOD_MS = 500
    }

    /*
    val backingStorage get() = storage

    private var recentUsers: Map<UUID, Long> = mapOf()

    fun onPunched(player: PlayerEntity) {
        if (world?.isClient != false || storage.isEmpty) {
            return
        }

        if (player.isSneaking) {
            storage.take(storage.currentInstance.maxCount.toLong())[0].giveTo(player)
        } else {
            storage.take(1)[0].giveTo(player)
        }

        markDirty()
        showContents(player)
    }

    fun onActivated(player: PlayerEntity) {
        if (world?.isClient != false) {
            return
        }

        recentUsers = recentUsers.filterValues { t -> System.currentTimeMillis() - t <= RIGHT_CLICK_PERIOD_MS }

        if (player.isSneaking) {
            if (storage.clearWhenEmpty) {
                storage.retainInstanceWhenEmpty()
                player.addChatMessage(TranslatableText("ui.stockpile.barrel.just_locked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.1f, 0.9f)
            } else {
                storage.clearInstanceWhenEmpty()
                player.addChatMessage(TranslatableText("ui.stockpile.barrel.just_unlocked"), true)
                player.playSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.1f, 0.9f)
            }

            markDirty()
            return
        }

        recentUsers = if (player.uuid in recentUsers && storage.instanceIsSet) {
            player.inventory.main.replaceAll { s -> storage.offer(s) }
            player.inventory.offHand.replaceAll { s -> storage.offer(s) }
            player.inventory.markDirty()

            recentUsers - player.uuid
        } else {
            val activeStack = player.getStackInHand(Hand.MAIN_HAND)
            val resultingStack = storage.offer(activeStack)

            player.setStackInHand(Hand.MAIN_HAND, resultingStack)
            player.inventory.markDirty()

            recentUsers + (player.uuid to System.currentTimeMillis())
        }

        markDirty()
        showContents(player)
    }

    private fun showContents(player: PlayerEntity) {
        player.addChatMessage(getContentDescription(), true)
    }

    override fun markDirty() {
        super.markDirty()
        invWrapper.markDirty()

        world?.apply {
            updateListeners(pos, getBlockState(pos), getBlockState(pos), 3)
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        return toTagWithoutWorldInfo(super.toTag(tag))
    }

    private fun toTagWithoutWorldInfo(tag: CompoundTag): CompoundTag {
        tag.put(STORED_ITEM_TAG, storage.currentInstance.toTag(CompoundTag()))
        tag.putLong(AMOUNT_STORED_TAG, storage.amountStored)
        tag.putBoolean(CLEAR_WHEN_EMPTY_TAG, storage.clearWhenEmpty)

        val upgradeTags = ListTag()
        appliedUpgrades.forEach { u -> upgradeTags.add(UpgradeRegistry.writeUpgrade(u)) }

        tag.put(UPGRADE_TAG, upgradeTags)

        return tag
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)

        if (tag == null) {
            Stockpile.LOGGER.warn("an item barrel at $pos is missing data and will be reset to defaults")
            return
        }

        fromTagWithoutWorldInfo(tag)
    }

    private fun fromTagWithoutWorldInfo(tag: CompoundTag) {
        val storedItem = ItemStack.fromTag(tag.getCompound(STORED_ITEM_TAG))
        val amountStored = if (storedItem.isEmpty) 0L else tag.getLong(AMOUNT_STORED_TAG)
        val clearWhenEmpty = tag.getBoolean(CLEAR_WHEN_EMPTY_TAG)

        val upgradeTags = tag.getList(UPGRADE_TAG, NbtType.COMPOUND).take(MAX_UPGRADES)

        appliedUpgrades =
            upgradeTags.mapNotNull { t -> (t as? CompoundTag)?.let { c -> UpgradeRegistry.readUpgrade(c) as? ItemBarrelUpgrade } }

        val capacityStacks = appliedUpgrades.fold(DEFAULT_CAPACITY_STACKS) { i, u -> u.upgradeMaxStacks(i) }

        storage = MutableMassItemStackStorage(
            capacityStacks,
            min(amountStored, capacityStacks.toLong() * storedItem.maxCount),
            storedItem,
            clearWhenEmpty
        )

        invAttribute.storage = storage
        invAttribute.voidExtraItems = appliedUpgrades.filterIsInstance<TrashUpgrade>().any()
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag = toTag(tag ?: CompoundTag())

    override fun fromClientTag(tag: CompoundTag?) = fromTag(tag ?: CompoundTag())

    override fun isUpgradeTypeAllowed(u: Upgrade): Boolean = u is ItemBarrelUpgrade

    override fun pushUpgrade(u: Upgrade) {
        if (u !is ItemBarrelUpgrade) {
            Stockpile.LOGGER.warn("attempted to apply an invalid upgrade (type ${u.id}) to an item barrel")
            Stockpile.LOGGER.debug("this is a bug! was isUpgradeTypeAllowed checked?")
            return
        }

        appliedUpgrades += u

        fromTagWithoutWorldInfo(toTagWithoutWorldInfo(CompoundTag()))
        markDirty()
    }

    override fun popUpgrade(): Upgrade {
        val result = appliedUpgrades.last()
        appliedUpgrades = appliedUpgrades.dropLast(1)

        fromTagWithoutWorldInfo(toTagWithoutWorldInfo(CompoundTag())) // TODO: what?
        markDirty()

        return result
    }

    fun toStack(stack: ItemStack) {
        toTagWithoutWorldInfo(stack.getOrCreateSubTag(STORED_BLOCK_ENTITY_TAG))
    }

    fun fromStack(stack: ItemStack) {
        fromTagWithoutWorldInfo(stack.getOrCreateSubTag(STORED_BLOCK_ENTITY_TAG))
    }

    fun getContentDescription(): Text {
        val f = NumberFormat.getInstance()

        return if (storage.isEmpty) {
            TranslatableText("ui.stockpile.barrel.contents_empty", f.format(storage.maxStacks))
        } else {
            TranslatableText(
                "ui.stockpile.barrel.contents",
                f.format(storage.amountStored),
                f.format(storage.capacity),
                storage.currentInstance.name.asFormattedString(),
                f.format(storage.amountStored / storage.currentInstance.maxCount),
                f.format(storage.maxStacks)
            )
        }
    }

     */
}
