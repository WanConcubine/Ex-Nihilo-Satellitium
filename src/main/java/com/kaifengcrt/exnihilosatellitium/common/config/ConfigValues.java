package com.kaifengcrt.exnihilosatellitium.common.config;

/**
 * Core-facing view of the common config.
 *
 * <p>Block entities read {@code ConfigValues.crucibleCapacity()} instead of the
 * NeoForge {@code ModConfigSpec}, so the same core files compile on 1.20.1
 * Forge; only {@code platform/config/ModConfig} is rewritten per version.</p>
 *
 * <p>The platform installs its {@link ConfigSource} once in the mod constructor,
 * before registries and worlds are created. Every getter delegates straight to
 * the source, so a config reload is visible on the next read.</p>
 */
public final class ConfigValues {

    /** {@code null} until {@link #install(ConfigSource)} has run. */
    private static ConfigSource source;

    private ConfigValues() {
    }

    /**
     * Installs the platform config. Called from the mod constructor; calling it
     * again replaces the source (useful for tests).
     */
    public static void install(final ConfigSource value) {
        if (value == null) {
            throw new NullPointerException("ConfigValues needs a non-null ConfigSource");
        }
        source = value;
    }

    private static ConfigSource source() {
        if (source == null) {
            throw new IllegalStateException(
                    "ConfigValues.install() has not run; install the platform config "
                            + "in the mod constructor before worlds load");
        }
        return source;
    }

    public static int crucibleCapacity() {
        return source().crucibleCapacity();
    }

    public static int crucibleMeltCapacity() {
        return source().crucibleMeltCapacity();
    }

    public static float crucibleHeatMultiplier() {
        return source().crucibleHeatMultiplier();
    }

    public static int machineTicksPerOperation() {
        return source().machineTicksPerOperation();
    }

    public static int machineEnergyPerTick() {
        return source().machineEnergyPerTick();
    }

    public static int machineEnergyCapacity() {
        return source().machineEnergyCapacity();
    }

    public static int machineBurnPerOperation() {
        return source().machineBurnPerOperation();
    }
}
