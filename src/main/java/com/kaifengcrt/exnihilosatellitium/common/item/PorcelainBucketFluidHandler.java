package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Fluid capability of the porcelain bucket: one tank, 1000 mB, any fluid.
 * Only one fluid type at a time, like a normal bucket.
 */
public final class PorcelainBucketFluidHandler implements IFluidHandlerItem {

    private final ItemStack container;

    public PorcelainBucketFluidHandler(final ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return this.container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(final int tank) {
        return PorcelainBucketItem.fluidOf(this.container);
    }

    @Override
    public int getTankCapacity(final int tank) {
        return PorcelainBucketItem.CAPACITY;
    }

    @Override
    public boolean isFluidValid(final int tank, final FluidStack stack) {
        return !stack.isEmpty();
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        final FluidStack current = getFluidInTank(0);
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) {
            return 0;
        }
        final int room = PorcelainBucketItem.CAPACITY - current.getAmount();
        final int toFill = Math.min(room, resource.getAmount());
        if (toFill <= 0) {
            return 0;
        }
        if (action.execute()) {
            PorcelainBucketItem.setContents(this.container,
                    new FluidStack(resource.getFluid(), current.getAmount() + toFill));
        }
        return toFill;
    }

    @Override
    public FluidStack drain(final FluidStack resource, final FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        final FluidStack current = getFluidInTank(0);
        if (current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource)) {
            return FluidStack.EMPTY;
        }
        return drain(Math.min(resource.getAmount(), current.getAmount()), action);
    }

    @Override
    public FluidStack drain(final int maxDrain, final FluidAction action) {
        final FluidStack current = getFluidInTank(0);
        if (current.isEmpty() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        final int drained = Math.min(maxDrain, current.getAmount());
        if (action.execute()) {
            final int left = current.getAmount() - drained;
            if (left <= 0) {
                PorcelainBucketItem.setContents(this.container, FluidStack.EMPTY);
            } else {
                PorcelainBucketItem.setContents(this.container, new FluidStack(current.getFluid(), left));
            }
        }
        return new FluidStack(current.getFluid(), drained);
    }
}
