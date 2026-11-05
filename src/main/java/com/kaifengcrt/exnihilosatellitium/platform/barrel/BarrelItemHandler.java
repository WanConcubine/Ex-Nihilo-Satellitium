package com.kaifengcrt.exnihilosatellitium.platform.barrel;

import com.kaifengcrt.exnihilosatellitium.common.blockentity.BarrelBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Pipe/hopper view of a barrel.
 *
 * <p>Slot 0 is the compost input: inserting a compostable item consumes it
 * immediately and credits its points. Slot 1 is the output: the ready dirt or
 * the solid reaction product (cobblestone / obsidian / ...), extracted by a
 * pipe or hopper. The item handler never stores a stack itself; the barrel's
 * own state remains the single source of truth.</p>
 *
 * <p>Platform glue (1.21.1 capability types); the 1.20.1 branch re-implements
 * this against {@code ForgeCapabilities.ITEM_HANDLER}.</p>
 */
public final class BarrelItemHandler implements IItemHandler {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int SLOTS = 2;

    private final BarrelBlockEntity barrel;

    public BarrelItemHandler(final BarrelBlockEntity barrel) {
        this.barrel = barrel;
    }

    @Override
    public int getSlots() {
        return SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(final int slot) {
        return slot == OUTPUT ? this.barrel.getOutputStack() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
        if (slot != INPUT || stack.isEmpty()) {
            return stack;
        }
        final int accepted = this.barrel.insertCompost(stack, stack.getCount(), simulate);
        if (accepted <= 0) {
            return stack;
        }
        final ItemStack remainder = stack.copy();
        remainder.shrink(accepted);
        return remainder;
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (slot != OUTPUT || amount <= 0) {
            return ItemStack.EMPTY;
        }
        final ItemStack preview = this.barrel.getOutputStack();
        if (preview.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (simulate) {
            preview.setCount(Math.min(amount, preview.getCount()));
            return preview;
        }
        return this.barrel.extractOutput(false);
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        return slot == INPUT && this.barrel.canInsertCompost(stack);
    }
}
