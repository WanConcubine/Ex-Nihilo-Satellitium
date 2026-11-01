package com.kaifengcrt.exnihilosatellitium.common.machine;

/**
 * FE buffer math for the automation machines.
 *
 * <p>Pure integers, no NeoForge energy types: the same class compiles on 1.20.1.
 * The platform exposes it to pipes through {@code platform/machine/MachineEnergy}
 * (an {@code EnergyStorage} adapter), and the machine entity drains it directly
 * — pipes must never extract from the buffer.</p>
 */
public final class MachineEnergyBuffer {

    /** Pipe-side input limit per tick. */
    public static final int MAX_RECEIVE = 1024;

    private final int capacity;
    private int energy;

    public MachineEnergyBuffer(final int capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public int capacity() {
        return this.capacity;
    }

    public int stored() {
        return this.energy;
    }

    /**
     * Fills the buffer, clamped by {@link #MAX_RECEIVE} and the free space.
     *
     * @return the amount that was (or, when {@code simulate}, would be) accepted
     */
    public int receive(final int amount, final boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        final int accepted = Math.min(Math.min(amount, MAX_RECEIVE), this.capacity - this.energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy += accepted;
        }
        return accepted;
    }

    /**
     * Internal consumption by the machine itself.
     *
     * @return {@code false} (and no change) when the buffer cannot pay
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

    /**
     * Direct write used when loading a save or a {@code /data merge}: the normal
     * {@link #receive} path clamps to {@link #MAX_RECEIVE} per call, which would
     * silently truncate a full buffer.
     */
    public void setStored(final int value) {
        this.energy = Math.max(0, Math.min(this.capacity, value));
    }
}
