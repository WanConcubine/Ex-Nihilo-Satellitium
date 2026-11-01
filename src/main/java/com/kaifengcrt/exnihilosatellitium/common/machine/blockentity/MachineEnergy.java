package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Machine energy buffer. {@link EnergyStorage#receiveEnergy} clamps to
 * {@code maxReceive}, which is right for pipes but wrong for loading a save or
 * a {@code /data merge} that contains the full buffer, so expose a direct setter.
 */
public class MachineEnergy extends EnergyStorage {

    public MachineEnergy(final int capacity) {
        super(capacity, 1024, 0);
    }

    public void setEnergyStored(final int value) {
        this.energy = Math.max(0, Math.min(this.capacity, value));
    }

    /**
     * Internal consumption. {@link EnergyStorage#extractEnergy} refuses to drain
     * when {@code maxExtract} is 0 (which is what keeps pipes from stealing the
     * buffer), so the machine needs its own door.
     */
    public boolean drain(final int amount) {
        if (amount <= 0) {
            return true;
        }
        if (this.energy < amount) {
            return false;
        }
        this.energy -= amount;
        return true;
    }
}
