package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Machine slots. Validation lives in {@link MachineBlockEntity#canPlaceInSlot}
 * so menus, hoppers and the machine tick use exactly one rule set.
 */
public class MachineInventory extends SimpleContainer {

    private final MachineBlockEntity owner;

    public MachineInventory(final MachineBlockEntity owner, final int size) {
        super(size);
        this.owner = owner;
    }

    public MachineBlockEntity owner() {
        return this.owner;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return this.owner.canPlaceInSlot(slot, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.owner.setChanged();
    }
}
