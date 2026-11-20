package com.kaifengcrt.exnihilosatellitium.platform.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.regex.Pattern;

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

    /** Same shape materials.json uses for {@code id}; keeps typos out of {@code ores.keep}. */
    private static final Pattern KEEP_ID = Pattern.compile("[a-z0-9_]{1,64}");

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue CRUCIBLE_CAPACITY;
    public static final ForgeConfigSpec.IntValue CRUCIBLE_MELT_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue CRUCIBLE_HEAT_MULTIPLIER;

    public static final ForgeConfigSpec.IntValue MACHINE_TICKS_PER_OPERATION;
    public static final ForgeConfigSpec.IntValue MACHINE_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.IntValue MACHINE_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue MACHINE_BURN_PER_OPERATION;

    public static final ForgeConfigSpec.BooleanValue ORES_HIDE_UNPROVIDED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ORES_KEEP;

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

        builder.comment("Automation machines (M10)").push("machine");
        MACHINE_TICKS_PER_OPERATION = builder
                .comment("Ticks one sift / crush takes before speed upgrades (default 100 = 5 s).")
                .defineInRange("ticksPerOperation", 100, 1, 2400);
        MACHINE_ENERGY_PER_TICK = builder
                .comment("FE consumed per working tick by electric machines (0 = free).",
                        "Default 20 FE/t: one operation costs 1600 FE before speed upgrades.")
                .defineInRange("energyPerTick", 20, 0, 10000);
        MACHINE_ENERGY_CAPACITY = builder
                .comment("Internal FE buffer of electric machines (default 48000).")
                .defineInRange("energyCapacity", 48000, 0, 2000000);
        MACHINE_BURN_PER_OPERATION = builder
                .comment("Vanilla burn ticks consumed per completed operation by fuel machines.",
                        "Default 200: one coal (1600) is eight operations.")
                .defineInRange("burnPerOperation", 200, 1, 20000);
        builder.pop();

        builder.comment("Ore materials").push("ores");
        ORES_HIDE_UNPROVIDED = builder
                .comment("Hide ore materials that no other mod supplies.",
                        "A material counts as unprovided when the mod has to register its own",
                        "fallback ingot: either its candidate list (tin/cobalt/...) has none of",
                        "its mods installed, or it lists no provider at all (platinum). Its",
                        "sift/hammer chain, creative-tab entries and JEI pages are dropped, so",
                        "e.g. cobalt and ardite stop showing up without Tinkers' Construct.",
                        "Iron / gold / copper keep their vanilla ingots and stay visible.",
                        "Takes effect on the next start (registries and creative tabs are",
                        "built once).")
                .define("hideUnprovided", false);
        ORES_KEEP = builder
                .comment("Material ids that stay visible even when no mod supplies them.",
                        "Classic content this mod owns outright lives here: platinum has no",
                        "provider mod at all, so without it the hideUnprovided switch would",
                        "delete platinum along with the unprovided metals. Ids are the",
                        "'id' field of materials.json (lowercase, underscores).",
                        "Only the hideUnprovided rule is affected; gems that are skipped at",
                        "registration cannot be brought back this way.")
                .defineListAllowEmpty("keep", List.of("platinum"),
                        o -> o instanceof String s && KEEP_ID.matcher(s).matches());
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

    public static boolean oresHideUnprovided() {
        // Datagen never loads configs; treat "not loaded yet" as the default (off).
        return SPEC.isLoaded() && ORES_HIDE_UNPROVIDED.get();
    }

    public static List<String> oresKeep() {
        if (!SPEC.isLoaded()) {
            return List.of("platinum");
        }
        return List.copyOf(ORES_KEEP.get());
    }
}
