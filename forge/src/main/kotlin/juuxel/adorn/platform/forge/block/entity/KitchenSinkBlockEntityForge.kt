package juuxel.adorn.platform.forge.block.entity

import juuxel.adorn.block.entity.KitchenSinkBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.sound.SoundCategory
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.event.GameEvent
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidAttributes
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.templates.FluidTank
import kotlin.math.min

class KitchenSinkBlockEntityForge(pos: BlockPos, state: BlockState) : KitchenSinkBlockEntity(pos, state) {
    val tank: FluidTank = object : FluidTank(FluidAttributes.BUCKET_VOLUME) {
        override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction?): FluidStack {
            return if (supportsInfiniteExtraction(world!!, fluid.fluid)) {
                FluidStack(fluid, min(fluidAmount, maxDrain))
            } else {
                super.drain(maxDrain, action)
            }
        }

        override fun onContentsChanged() {
            markDirtyAndSync()
        }
    }

    private val tankHolder = LazyOptional.of { tank }

    override fun interactWithItem(stack: ItemStack, player: PlayerEntity, hand: Hand): Boolean {
        val w = world!!

        if (tank.space > 0) {
            // The player in the tryEmpty/FillContainer calls is only used for sound.
            val result = FluidUtil.tryEmptyContainer(stack, tank, tank.space, null, true)

            if (result.isSuccess) {
                if (!w.isClient) {
                    w.emitGameEvent(GameEvent.FLUID_PLACE, pos)
                    player.playSound(getEmptySound(tank.fluid.fluid, stack).event, SoundCategory.BLOCKS, 1f, 1f)
                }

                setStackOrInsert(player, hand, result.result)
                markDirtyAndSync()
                return true
            }
        }

        // Store before filling the item from the tank
        val tankFluid = tank.fluid.fluid
        val result = FluidUtil.tryFillContainer(stack, tank, tank.fluidAmount, null, true)

        if (result.isSuccess) {
            if (!w.isClient) {
                w.emitGameEvent(GameEvent.FLUID_PICKUP, pos)
                player.playSound(getFillSound(tankFluid, stack).event, SoundCategory.BLOCKS, 1f, 1f)
            }

            setStackOrInsert(player, hand, result.result)
            markDirtyAndSync()
            return true
        }

        // Special case bottles since they don't have a fluid handler.
        if (stack.isOf(Items.GLASS_BOTTLE)) {
            // Since it's water, note that it won't drain anything. (infinite fluid)
            if (tank.fluid.isFluidEqual(PLAIN_WATER) && tank.fluidAmount >= BOTTLE_LITRES) {
                val bottle = ItemStack(Items.POTION)
                PotionUtil.setPotion(bottle, Potions.WATER)
                setStackOrInsert(player, hand, bottle)
                if (!world!!.isClient) {
                    player.playSound(getFillSound(Fluids.WATER, stack).event, SoundCategory.BLOCKS, 1f, 1f)
                }
                return true
            }
        } else if (stack.isOf(Items.POTION)) {
            val spaceForWater = tank.fluid.isEmpty || (tank.fluid.isFluidEqual(PLAIN_WATER) && tank.space >= BOTTLE_LITRES)

            if (spaceForWater && PotionUtil.getPotion(stack) == Potions.WATER) {
                val fluid = PLAIN_WATER.copy()
                fluid.amount = BOTTLE_LITRES
                tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE)
                setStackOrInsert(player, hand, ItemStack(Items.GLASS_BOTTLE))
                markDirtyAndSync()
                if (!world!!.isClient) {
                    player.playSound(getEmptySound(Fluids.WATER, stack).event, SoundCategory.BLOCKS, 1f, 1f)
                }
                return true
            }
        }

        return false
    }

    private fun setStackOrInsert(player: PlayerEntity, hand: Hand, stack: ItemStack) {
        val current = player.getStackInHand(hand)
        current.decrement(1)

        if (current.isEmpty) {
            player.setStackInHand(hand, stack)
        } else {
            player.inventory.offerOrDrop(stack)
        }
    }

    override fun clearFluidsWithSponge(): Boolean {
        if (!tank.fluid.fluid.isIn(FluidTags.WATER) || tank.fluid.amount == 0) return false

        tank.fluid.amount = 0
        markDirtyAndSync()
        return true
    }

    override fun getFillSound(fluid: Fluid, stack: ItemStack): FluidItemSound =
        super.getFillSound(fluid, stack).orElse(fluid.attributes.fillSound)

    override fun getEmptySound(fluid: Fluid, stack: ItemStack): FluidItemSound =
        super.getEmptySound(fluid, stack).orElse(fluid.attributes.emptySound)

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        tank.readFromNBT(nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        tank.writeToNBT(nbt)
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return tankHolder.cast()
        }

        return super.getCapability(cap, side)
    }

    override fun calculateComparatorOutput(): Int =
        if (tank.isEmpty) 0
        else 1 + MathHelper.floor(14 * tank.fluidAmount.toFloat() / tank.capacity.toFloat())

    companion object {
        // Bottles are 250 l in Adorn *on Forge*.
        private const val BOTTLE_LITRES = 250
        private val PLAIN_WATER = FluidStack(Fluids.WATER, 1)
    }
}
