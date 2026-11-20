package com.kaifengcrt.exnihilosatellitium.common.config;

/**
 * Values the core gameplay code is allowed to read from the common config.
 *
 * <p>This is the version-neutral half of the config facade: the platform
 * implements it on top of its own config spec ({@code platform/config/ExConfig}
 * on 1.20.1 Forge). Core never imports {@code ForgeConfigSpec}.</p>
 *
 * <p>Implementations must read the live value on every call, not cache it, so a
 * config reload takes effect without restarting the game.</p>
 */
public interface ConfigSource {

    int crucibleCapacity();

    int crucibleMeltCapacity();

    /** Multiplier applied to every crucible heat source's mB/tick. */
    float crucibleHeatMultiplier();

    int machineTicksPerOperation();

    int machineEnergyPerTick();

    int machineEnergyCapacity();

    int machineBurnPerOperation();

    /**
     * When true, ore materials whose candidate mods are all missing are dropped
     * from the sift/hammer tables, the creative tab and JEI.
     */
    boolean oresHideUnprovided();
}
