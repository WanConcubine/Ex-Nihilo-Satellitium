package com.kaifengcrt.exnihilosatellitium.common.machine.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntSupplier;

/**
 * A 12-slot window into one page of the machine output buffer.
 *
 * <p>The machine inventory keeps every page pre-allocated; this view only maps
 * {@code viewSlot -> outputStart + page * 12 + viewSlot}. Every read and write
 * goes straight through to the real inventory, so the server and client menus
 * use the same slot objects and the page switch never moves items around.</p>
 */
final class PagedOutputContainer implements Container {

    private final Container target;
    private final int outputStart;
    private final int pageSize;
    private final IntSupplier page;

    PagedOutputContainer(final Container target, final int outputStart, final int pageSize,
                         final IntSupplier page) {
        this.target = target;
        this.outputStart = outputStart;
        this.pageSize = pageSize;
        this.page = page;
    }

    private int physical(final int slot) {
        return this.outputStart + this.page.getAsInt() * this.pageSize + slot;
    }

    @Override
    public int getContainerSize() {
        return this.pageSize;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.pageSize; slot++) {
            if (!this.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slot) {
        return this.target.getItem(this.physical(slot));
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        return this.target.removeItem(this.physical(slot), amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slot) {
        return this.target.removeItemNoUpdate(this.physical(slot));
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        this.target.setItem(this.physical(slot), stack);
    }

    @Override
    public int getMaxStackSize() {
        return this.target.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        this.target.setChanged();
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < this.pageSize; slot++) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.target.stillValid(player);
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return this.target.canPlaceItem(this.physical(slot), stack);
    }
}
