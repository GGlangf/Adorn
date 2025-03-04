@file:Suppress("UnstableApiUsage")

package juuxel.adorn.block.entity

import com.google.common.base.Predicates
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.event.GameEvent
import kotlin.math.min

class KitchenSinkBlockEntityFabric(pos: BlockPos, state: BlockState) : KitchenSinkBlockEntity(pos, state) {
    val storage: SingleVariantStorage<FluidVariant> = object : SingleVariantStorage<FluidVariant>() {
        override fun extract(extractedVariant: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
            StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount)

            // Support for infinite extraction
            return if (amount > 0 && supportsInfiniteExtraction(world!!, variant.fluid) && extractedVariant == variant) {
                return min(amount, maxAmount)
            } else {
                super.extract(extractedVariant, maxAmount, transaction)
            }
        }

        override fun getCapacity(variant: FluidVariant): Long =
            FluidConstants.BUCKET

        override fun getBlankVariant(): FluidVariant =
            FluidVariant.blank()

        override fun onFinalCommit() {
            markDirtyAndSync()
        }
    }

    override fun interactWithItem(stack: ItemStack, player: PlayerEntity, hand: Hand): Boolean {
        val w = world!!
        val itemStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand)) ?: return false
        val hasSpace = storage.amount < storage.capacity

        if (hasSpace) {
            val moved = StorageUtil.move(itemStorage, storage, Predicates.alwaysTrue(), Long.MAX_VALUE, null)

            if (moved > 0) {
                if (!w.isClient) {
                    player.playSound(getEmptySound(storage.variant.fluid, stack).event, SoundCategory.BLOCKS, 1f, 1f)
                    w.emitGameEvent(GameEvent.FLUID_PLACE, pos)
                }

                markDirtyAndSync()
                return true
            }
        }

        val moved = StorageUtil.move(storage, itemStorage, Predicates.alwaysTrue(), Long.MAX_VALUE, null)

        if (moved > 0) {
            if (!w.isClient) {
                player.playSound(getFillSound(storage.variant.fluid, stack).event, SoundCategory.BLOCKS, 1f, 1f)
                w.emitGameEvent(GameEvent.FLUID_PICKUP, pos)
            }

            markDirtyAndSync()
            return true
        }

        return false
    }

    override fun clearFluidsWithSponge(): Boolean {
        if (!storage.variant.fluid.isIn(FluidTags.WATER) || storage.amount == 0L) return false
        storage.amount = 0L
        markDirtyAndSync()
        return true
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        storage.variant = FluidVariant.fromNbt(nbt.getCompound(NBT_FLUID))
        storage.amount = nbt.getLong(NBT_VOLUME)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.put(NBT_FLUID, storage.variant.toNbt())
        nbt.putLong(NBT_VOLUME, storage.amount)
    }

    override fun calculateComparatorOutput(): Int =
        if (storage.amount == 0L) 0
        else 1 + MathHelper.floor(14 * storage.amount.toDouble() / storage.capacity.toDouble())

    companion object {
        val FLUID_STORAGE_PROVIDER: BlockApiLookup.BlockApiProvider<Storage<FluidVariant>, Direction> =
            BlockApiLookup.BlockApiProvider { _, _, _, blockEntity, _ ->
                (blockEntity as? KitchenSinkBlockEntityFabric)?.storage
            }

        private const val NBT_FLUID = "Fluid"
        private const val NBT_VOLUME = "Volume"
    }
}
