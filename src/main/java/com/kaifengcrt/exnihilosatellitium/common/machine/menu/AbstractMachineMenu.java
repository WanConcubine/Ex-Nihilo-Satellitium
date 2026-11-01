package com.kaifengcrt.exnihilosatellitium.common.machine.menu;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Shared slot layout for the automation machines.
 *
 * <p>Machine area (top 84 px): tool + input on the left, a 3x3 output buffer on
 * the right. Electric machines add four upgrade slots along the bottom and an
 * energy bar; fuel machines add one fuel slot and a flame gauge instead.</p>
 */
public abstract class AbstractMachineMenu extends AbstractContainerMenu {

    public static final int PLAYER_INVENTORY_START = 0;

    protected final MachineBlockEntity owner;
    protected final Container machine;
    protected final ContainerData data;
    protected final boolean electric;

    protected AbstractMachineMenu(final MenuType<?> type, final int id,
                                  final Inventory playerInventory,
                                  final MachineBlockEntity owner, final boolean electric) {
        super(type, id);
        this.owner = owner;
        this.electric = electric;
        this.machine = owner != null ? owner.getInventory()
                : new SimpleContainer(electric ? 15 : 12);
        this.data = owner != null ? owner : new SimpleContainerData(MachineBlockEntity.DATA_COUNT);

        this.addSlot(new Slot(this.machine, MachineBlockEntity.TOOL_SLOT, 26, 22));
        this.addSlot(new Slot(this.machine, MachineBlockEntity.INPUT_SLOT, 26, 46));

        if (electric) {
            for (int i = 0; i < MachineBlockEntity.UPGRADE_SLOTS; i++) {
                this.addSlot(new Slot(this.machine,
                        MachineBlockEntity.UPGRADE_SLOT_START + i, 26 + i * 18, 64));
            }
        } else {
            this.addSlot(new Slot(this.machine, MachineBlockEntity.FUEL_SLOT, 26, 64));
        }

        final int outputStart = electric ? 6 : 3;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlot(new Slot(this.machine, outputStart + row * 3 + column,
                        98 + column * 18, 22 + row * 18));
            }
        }

        this.addDataSlots(this.data);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public MachineBlockEntity owner() {
        return this.owner;
    }

    public ContainerData data() {
        return this.data;
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.owner == null || this.owner.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = slot.getItem();
        final ItemStack copy = stack.copy();
        final int machineSlots = this.machine.getContainerSize();
        if (index < machineSlots) {
            if (!this.moveItemStackTo(stack, machineSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    /** Client side: locate the machine BE from the block position sent by the server. */
    protected static MachineBlockEntity blockEntityAt(final Inventory playerInventory,
                                                      final BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof MachineBlockEntity machine
                ? machine : null;
    }
}
