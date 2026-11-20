package com.kaifengcrt.exnihilosatellitium.platform.config;

import com.kaifengcrt.exnihilosatellitium.common.config.ConfigSource;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Common config spec (NeoForge). Everything here is read at runtime, so a config
 * reload is enough for new values (the crucible clamps its tank to the current
 * capacity).
 *
 * <p>This is the platform half of the config facade: core reads
 * {@link com.kaifengcrt.exnihilosatellitium.common.config.ConfigValues}, which
 * delegates to {@link #INSTANCE}. A 1.20.1 port only swaps this class for a
 * {@code ForgeConfigSpec} implementation of {@link ConfigSource}.</p>
 */
public final class ModConfig implements ConfigSource {

    /** Same shape materials.json uses for {@code id}; keeps typos out of {@code ores.keep}. */
    private static final Pattern KEEP_ID = Pattern.compile("[a-z0-9_]{1,64}");

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue CRUCIBLE_CAPACITY;
    public static final ModConfigSpec.IntValue CRUCIBLE_MELT_CAPACITY;
    public static final ModConfigSpec.DoubleValue CRUCIBLE_HEAT_MULTIPLIER;

    public static final ModConfigSpec.IntValue MACHINE_TICKS_PER_OPERATION;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_CAPACITY;
    public static final ModConfigSpec.IntValue MACHINE_BURN_PER_OPERATION;

    public static final ModConfigSpec.BooleanValue ORES_HIDE_UNPROVIDED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ORES_KEEP;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Crucible").push("crucible");
        CRUCIBLE_CAPACITY = builder
                .translation("exnihilosatellitium.config.crucible.capacity")
                .comment("Crucible tank capacity in mB (default 8000, classic 1.7.10 used 10000).")
                .defineInRange("capacity", 8000, 1000, 64000);
        CRUCIBLE_MELT_CAPACITY = builder
                .translation("exnihilosatellitium.config.crucible.melt_capacity")
                .comment("Solid (melt) capacity in mB. One cobblestone/sand unit is 250/100;",
                        "default 2000 = eight cobblestone blocks.")
                .defineInRange("meltCapacity", 2000, 250, 64000);
        CRUCIBLE_HEAT_MULTIPLIER = builder
                .translation("exnihilosatellitium.config.crucible.heat_speed_multiplier")
                .comment("Multiplier applied to every heat source's mB/tick.",
                        "1.0 = use the JSON speed as-is.")
                .defineInRange("heatSpeedMultiplier", 1.0D, 0.1D, 10.0D);
        builder.pop();

        builder.comment("Automation machines (M10)").push("machine");
        MACHINE_TICKS_PER_OPERATION = builder
                .translation("exnihilosatellitium.config.machine.ticks_per_operation")
                .comment("Ticks one sift / crush takes before speed upgrades (default 100 = 5 s).")
                .defineInRange("ticksPerOperation", 100, 1, 2400);
        MACHINE_ENERGY_PER_TICK = builder
                .translation("exnihilosatellitium.config.machine.energy_per_tick")
                .comment("FE consumed per working tick by electric machines (0 = free).",
                        "Default 20 FE/t: one operation costs 1600 FE before speed upgrades.")
                .defineInRange("energyPerTick", 20, 0, 10000);
        MACHINE_ENERGY_CAPACITY = builder
                .translation("exnihilosatellitium.config.machine.energy_capacity")
                .comment("Internal FE buffer of electric machines (default 48000).")
                .defineInRange("energyCapacity", 48000, 0, 2000000);
        MACHINE_BURN_PER_OPERATION = builder
                .translation("exnihilosatellitium.config.machine.burn_per_operation")
                .comment("Vanilla burn ticks consumed per completed operation by fuel machines.",
                        "Default 200: one coal (1600) is eight operations.")
                .defineInRange("burnPerOperation", 200, 1, 20000);
        builder.pop();

        builder.comment("Ore materials").push("ores");
        ORES_HIDE_UNPROVIDED = builder
                .translation("exnihilosatellitium.config.ores.hide_unprovided")
                .comment("Hide ore materials that no other mod supplies.",
                        "A material counts as unprovided when the mod has to register its own",
                        "fallback ingot: either its candidate list (tin/cobalt/...) has none of",
                        "its mods installed, or it lists no provider at all (platinum). Its",
                        "sift/hammer chain, creative-tab entries and JEI pages are dropped, so",
                        "e.g. cobalt and ardite stop showing up without Tinkers' Construct.",
                        "Iron / gold / copper keep their vanilla ingots and stay visible.",
                        "Takes effect on the next start (registries and creative tabs are",
                        "built once).")
                .gameRestart()
                .define("hideUnprovided", false);
        ORES_KEEP = builder
                .translation("exnihilosatellitium.config.ores.keep")
                .comment("Material ids that stay visible even when no mod supplies them.",
                        "Classic content this mod owns outright lives here: platinum has no",
                        "provider mod at all, so without it the hideUnprovided switch would",
                        "delete platinum along with the unprovided metals. Ids are the",
                        "'id' field of materials.json (lowercase, underscores).",
                        "Only the hideUnprovided rule is affected; gems that are skipped at",
                        "registration cannot be brought back this way.")
                .gameRestart()
                .defineListAllowEmpty("keep", List.of("platinum"), () -> "platinum",
                        o -> o instanceof String s && KEEP_ID.matcher(s).matches());
        builder.pop();

        SPEC = builder.build();
    }

    /** The source installed into {@code ConfigValues} by the mod constructor. */
    public static final ModConfig INSTANCE = new ModConfig();

    private ModConfig() {
    }

    @Override
    public int crucibleCapacity() {
        return CRUCIBLE_CAPACITY.get();
    }

    @Override
    public int crucibleMeltCapacity() {
        return CRUCIBLE_MELT_CAPACITY.get();
    }

    @Override
    public float crucibleHeatMultiplier() {
        return (float) (double) CRUCIBLE_HEAT_MULTIPLIER.get();
    }

    @Override
    public int machineTicksPerOperation() {
        return MACHINE_TICKS_PER_OPERATION.get();
    }

    @Override
    public int machineEnergyPerTick() {
        return MACHINE_ENERGY_PER_TICK.get();
    }

    @Override
    public int machineEnergyCapacity() {
        return MACHINE_ENERGY_CAPACITY.get();
    }

    @Override
    public int machineBurnPerOperation() {
        return MACHINE_BURN_PER_OPERATION.get();
    }

    @Override
    public boolean oresHideUnprovided() {
        // Datagen never loads configs; treat "not loaded yet" as the default (off).
        return SPEC.isLoaded() && ORES_HIDE_UNPROVIDED.get();
    }

    @Override
    public List<String> oresKeep() {
        if (!SPEC.isLoaded()) {
            return List.of("platinum");
        }
        return List.copyOf(ORES_KEEP.get());
    }
}
