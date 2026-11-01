package com.kaifengcrt.exnihilosatellitium.common.machine.menu;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Menu of the fuel-burning machine (one fuel slot, no upgrades). */
public class FuelMachineMenu extends AbstractMachineMenu {

    public FuelMachineMenu(final int id, final Inventory playerInventory,
                           final MachineBlockEntity owner) {
        super(ModMenuTypes.FUEL_MACHINE.get(), id, playerInventory, owner, false);
    }

    /** Network factory: the block position is the only data needed. */
    public FuelMachineMenu(final int id, final Inventory playerInventory,
                           final RegistryFriendlyByteBuf buffer) {
        this(id, playerInventory, blockEntityAt(playerInventory, buffer.readBlockPos()));
    }
}
