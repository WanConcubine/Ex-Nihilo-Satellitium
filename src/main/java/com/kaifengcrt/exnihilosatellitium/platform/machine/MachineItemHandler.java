package com.kaifengcrt.exnihilosatellitium.platform.machine;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Pipe/hopper view of a machine. Insertion follows the same slot rules as the
 * menu, but extraction is limited to the output buffer — otherwise a hopper
 * under the machine would happily pull out the mesh, hammer and upgrades.
 *
 * <p>Platform glue (1.21.1 capability types); a 1.20.1 port re-implements this
 * against {@code ForgeCapabilities.ITEM_HANDLER}.</p>
 */
public class MachineItemHandler implements IItemHandlerModifiable {

    private final MachineBlockEntity owner;
    private final MachineInventory inventory;

    public MachineItemHandler(final MachineBlockEntity owner) {
        this.owner = owner;
        this.inventory = owner.getInventory();
    }

    @Override
    public int getSlots() {
        // Only the live pages are exposed. The physical inventory pre-allocates
        // all eight possible pages, so this count changes with the capacity
        // upgrade; pipes still just see one flat list of output slots.
        return this.owner.outputSlotEnd();
    }

    @Override
    public ItemStack getStackInSlot(final int slot) {
        if (slot < 0 || slot >= this.getSlots()) {
            return ItemStack.EMPTY;
        }
        return this.inventory.getItem(slot);
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack) {
        if (slot < 0 || slot >= this.getSlots()) {
            return;
        }
        this.inventory.setItem(slot, stack);
    }

    @Override
    public int getSlotLimit(final int slot) {
        if (slot < 0 || slot >= this.getSlots()) {
            return 0;
        }
        if (this.owner.isElectric()) {
            if (this.owner.kind() == MachineKind.HAMMER
                    && slot == MachineBlockEntity.UPGRADE_SLOT_START + 1) {
                return 1;
            }
            if (slot == MachineBlockEntity.CAPACITY_SLOT) {
                return MachineBlockEntity.MAX_CAPACITY_PAGES;
            }
        }
        return Math.min(64, this.inventory.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        if (slot < 0 || slot >= this.getSlots()) {
            return false;
        }
        return this.inventory.canPlaceItem(slot, stack);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
        if (stack.isEmpty() || !this.isItemValid(slot, stack)) {
            return stack;
        }
        final ItemStack existing = this.inventory.getItem(slot);
        final int limit = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
        if (existing.isEmpty()) {
            final int moved = Math.min(limit, stack.getCount());
            if (!simulate) {
                this.inventory.setItem(slot, stack.copyWithCount(moved));
            }
            return moved >= stack.getCount() ? ItemStack.EMPTY
                    : stack.copyWithCount(stack.getCount() - moved);
        }
        if (!ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }
        final int room = limit - existing.getCount();
        if (room <= 0) {
            return stack;
        }
        final int moved = Math.min(room, stack.getCount());
        if (!simulate) {
            existing.grow(moved);
            this.inventory.setItem(slot, existing);
        }
        return moved >= stack.getCount() ? ItemStack.EMPTY
                : stack.copyWithCount(stack.getCount() - moved);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (amount <= 0 || !this.isOutput(slot)) {
            return ItemStack.EMPTY;
        }
        final ItemStack existing = this.inventory.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final int moved = Math.min(amount, existing.getCount());
        final ItemStack result = existing.copyWithCount(moved);
        if (!simulate) {
            existing.shrink(moved);
            this.inventory.setItem(slot, existing.isEmpty() ? ItemStack.EMPTY : existing);
        }
        return result;
    }

    private boolean isOutput(final int slot) {
        return slot >= this.owner.outputSlotStart() && slot < this.owner.outputSlotEnd();
    }
}
