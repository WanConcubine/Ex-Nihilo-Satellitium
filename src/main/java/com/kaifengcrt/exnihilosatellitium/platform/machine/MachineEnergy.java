package com.kaifengcrt.exnihilosatellitium.platform.machine;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineEnergyBuffer;
import net.minecraftforge.energy.EnergyStorage;

/**
 * Capability-facing FE adapter for a machine's core {@link MachineEnergyBuffer}.
 *
 * <p>Pipes may only insert, never extract: the machine itself drains the buffer.
 * 1.20.1 exposes energy through {@code ForgeCapabilities.ENERGY} and
 * {@link EnergyStorage}.</p>
 */
public final class MachineEnergy extends EnergyStorage {

    private final MachineEnergyBuffer buffer;

    public MachineEnergy(final MachineEnergyBuffer buffer) {
        super(buffer.capacity(), MachineEnergyBuffer.MAX_RECEIVE, 0);
        this.buffer = buffer;
    }

    @Override
    public int receiveEnergy(final int toReceive, final boolean simulate) {
        return this.buffer.receive(toReceive, simulate);
    }

    @Override
    public int extractEnergy(final int toExtract, final boolean simulate) {
        // maxExtract is 0 as well; keeping the override makes the intent explicit.
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.buffer.stored();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.buffer.capacity();
    }
}
