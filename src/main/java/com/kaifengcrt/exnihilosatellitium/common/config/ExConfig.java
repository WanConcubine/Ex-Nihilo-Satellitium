package com.kaifengcrt.exnihilosatellitium.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config for the 1.20.1 branch.
 *
 * <p>Mirrors the {@code crucible.*} block of the 1.21.1 config so both versions
 * expose the same knobs (the 1.21.1 branch hides them behind the
 * {@code ConfigValues} facade; this branch has a single platform, so the spec is
 * read directly). Values are live: the crucible clamps its tank to the current
 * capacity while ticking, so editing the file and running {@code /reload} is
 * enough.</p>
 */
public final class ExConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue CRUCIBLE_CAPACITY;
    public static final ForgeConfigSpec.IntValue CRUCIBLE_MELT_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_HEAT_MULTIPLIER;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Crucible").push("crucible");
        CRUCIBLE_CAPACITY = builder
                .comment("Crucible tank capacity in mB (default 8000, classic 1.7.10 used 10000).")
                .defineInRange("capacity", 8000, 1000, 64000);
        CRUCIBLE_MELT_CAPACITY = builder
                .comment("Solid (melt) capacity in mB. One cobblestone/sand unit is 250/100;",
                        "default 2000 = eight cobblestone blocks.")
                .defineInRange("meltCapacity", 2000, 250, 64000);
        CRUCIBLE_HEAT_MULTIPLIER = builder
                .comment("Multiplier applied to every heat source's mB/tick.",
                        "1.0 = use the JSON speed as-is.")
                .defineInRange("heatSpeedMultiplier", 1.0D, 0.1D, 10.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private ExConfig() {
    }

    public static int crucibleCapacity() {
        return CRUCIBLE_CAPACITY.get();
    }

    public static int crucibleMeltCapacity() {
        return CRUCIBLE_MELT_CAPACITY.get();
    }

    public static float crucibleHeatMultiplier() {
        return (float) (double) CRUCIBLE_HEAT_MULTIPLIER.get();
    }
}
