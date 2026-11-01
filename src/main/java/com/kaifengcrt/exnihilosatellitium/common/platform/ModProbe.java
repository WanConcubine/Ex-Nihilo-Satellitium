package com.kaifengcrt.exnihilosatellitium.common.platform;

/**
 * Minimal "what is loaded" view the core needs.
 *
 * <p>Core code (material table, compat probing) must not call NeoForge's
 * {@code ModList}: 1.20.1 Forge has a different loader package and a slightly
 * different API. The platform implements this interface once
 * ({@code platform/NeoForgeModProbe} on 1.21.1).</p>
 */
public interface ModProbe {

    /** Probe used before the platform installs the real one: nothing is loaded. */
    ModProbe NONE = new ModProbe() {
        @Override
        public boolean isLoaded(final String modId) {
            return false;
        }

        @Override
        public String version(final String modId) {
            return "";
        }
    };

    boolean isLoaded(String modId);

    /** Mod version string, or {@code ""} when the mod is absent. */
    String version(String modId);
}
