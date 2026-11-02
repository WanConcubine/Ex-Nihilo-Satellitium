package com.kaifengcrt.exnihilosatellitium.common.platform;

import java.util.Objects;

/**
 * Static access point for the installed {@link ModProbe}.
 *
 * <p>Core asks {@code Platform.isModLoaded("mekanism")} instead of
 * {@code ModList.get().isLoaded(...)}; the platform installs the probe in the
 * mod constructor. Before installation every query answers "not loaded", which
 * is the safe answer for optional-integration probing.</p>
 */
public final class Platform {

    private static volatile ModProbe probe = ModProbe.NONE;

    private Platform() {
    }

    /** Installs the platform probe; called from the mod constructor. */
    public static void install(final ModProbe value) {
        probe = Objects.requireNonNull(value, "ModProbe");
    }

    public static boolean isModLoaded(final String modId) {
        return probe.isLoaded(modId);
    }

    /** Mod version string, or {@code ""} when the mod is absent. */
    public static String modVersion(final String modId) {
        return probe.version(modId);
    }
}
