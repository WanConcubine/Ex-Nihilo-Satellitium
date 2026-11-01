package com.kaifengcrt.exnihilosatellitium.common.machine.menu;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/** Menu of the FE-powered machine (four upgrade slots + energy bar). */
public class ElectricMachineMenu extends AbstractMachineMenu {

    public ElectricMachineMenu(final int id, final Inventory playerInventory,
                               final MachineBlockEntity owner) {
        super(ModMenuTypes.ELECTRIC_MACHINE.get(), id, playerInventory, owner,
                owner != null ? owner.kind() : MachineKind.SIEVE, true);
    }

    private ElectricMachineMenu(final int id, final Inventory playerInventory,
                                final ClientContext context) {
        super(ModMenuTypes.ELECTRIC_MACHINE.get(), id, playerInventory,
                context.owner(), context.kind(), true);
    }

    /** Network factory: the block position is the only data needed. */
    public ElectricMachineMenu(final int id, final Inventory playerInventory,
                               final FriendlyByteBuf buffer) {
        this(id, playerInventory, clientContext(playerInventory, buffer));
    }
}
