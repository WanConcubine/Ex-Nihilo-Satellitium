package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Fuel-burning machine: furnace fuel slot, no upgrades. */
public class FuelMachineBlockEntity extends MachineBlockEntity {

    public FuelMachineBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.FUEL_MACHINE.get(), pos, state, false);
    }
}
