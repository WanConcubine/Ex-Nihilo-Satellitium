package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** FE-powered machine: upgrade slots, energy buffer. */
public class ElectricMachineBlockEntity extends MachineBlockEntity {

    public ElectricMachineBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.ELECTRIC_MACHINE.get(), pos, state, true);
    }
}
