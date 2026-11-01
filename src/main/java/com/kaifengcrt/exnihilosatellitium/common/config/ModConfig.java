package com.kaifengcrt.exnihilosatellitium.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config. Everything here is read at runtime, so a config reload is
 * enough for new values (the crucible clamps its tank to the current capacity).
 */
public final class ModConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue CRUCIBLE_CAPACITY;
    public static final ModConfigSpec.IntValue CRUCIBLE_MELT_CAPACITY;
    public static final ModConfigSpec.DoubleValue CRUCIBLE_HEAT_MULTIPLIER;

    public static final ModConfigSpec.IntValue MACHINE_TICKS_PER_OPERATION;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue MACHINE_BURN_PER_OPERATION;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

        builder.comment("Automation machines (M10)").push("machine");
        MACHINE_TICKS_PER_OPERATION = builder
                .comment("Ticks one sift / crush takes before speed upgrades (default 80 = 4 s).")
                .defineInRange("ticksPerOperation", 80, 1, 2400);
        MACHINE_ENERGY_PER_TICK = builder
                .comment("FE consumed per working tick by electric machines (0 = free).",
                        "Default 20 FE/t: one operation costs 1600 FE before speed upgrades.")
                .defineInRange("energyPerTick", 20, 0, 10000);
        MACHINE_ENERGY_CAPACITY = builder
                .comment("Internal FE buffer of electric machines.")
                .defineInRange("energyCapacity", 20000, 0, 2000000);
        MACHINE_BURN_PER_OPERATION = builder
                .comment("Vanilla burn ticks consumed per completed operation by fuel machines.",
                        "Default 200: one coal (1600) is eight operations.")
                .defineInRange("burnPerOperation", 200, 1, 20000);
        builder.pop();

        SPEC = builder.build();
    }

    private ModConfig() {
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

    public static int machineTicksPerOperation() {
        return MACHINE_TICKS_PER_OPERATION.get();
    }

    public static int machineEnergyPerTick() {
        return MACHINE_ENERGY_PER_TICK.get();
    }

    public static int machineEnergyCapacity() {
        return MACHINE_ENERGY_CAPACITY.get();
    }

    public static int machineBurnPerOperation() {
        return MACHINE_BURN_PER_OPERATION.get();
    }
}
