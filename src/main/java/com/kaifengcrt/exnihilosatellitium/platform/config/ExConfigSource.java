package com.kaifengcrt.exnihilosatellitium.platform.config;

import com.kaifengcrt.exnihilosatellitium.common.config.ConfigSource;

/**
 * {@link ConfigSource} adapter over the Forge {@link ExConfig} spec.
 *
 * <p>Installed once in the mod constructor so core code never touches
 * {@code ForgeConfigSpec}. Every method forwards to the live spec value.</p>
 */
public final class ExConfigSource implements ConfigSource {

    public static final ExConfigSource INSTANCE = new ExConfigSource();

    private ExConfigSource() {
    }

    @Override
    public int crucibleCapacity() {
        return ExConfig.crucibleCapacity();
    }

    @Override
    public int crucibleMeltCapacity() {
        return ExConfig.crucibleMeltCapacity();
    }

    @Override
    public float crucibleHeatMultiplier() {
        return ExConfig.crucibleHeatMultiplier();
    }

    @Override
    public int machineTicksPerOperation() {
        return ExConfig.machineTicksPerOperation();
    }

    @Override
    public int machineEnergyPerTick() {
        return ExConfig.machineEnergyPerTick();
    }

    @Override
    public int machineEnergyCapacity() {
        return ExConfig.machineEnergyCapacity();
    }

    @Override
    public int machineBurnPerOperation() {
        return ExConfig.machineBurnPerOperation();
    }

    @Override
    public boolean oresHideUnprovided() {
        return ExConfig.oresHideUnprovided();
    }
}
