package com.kaifengcrt.exnihilosatellitium.common.table;

import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipes;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostRegistry;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDrops;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleHeat;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleRecipes;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipes;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveDrop;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Stable, line-oriented view of the effective tables.
 *
 * <p>Used by {@code /exnihilo dump <table>}. Every line is
 * {@code <table> <key> ...}, and lines are sorted by their text, so two runs
 * with the same inputs produce byte-identical output. That is what lets the
 * three customization paths be compared: the same fixture expressed as a
 * datapack or as a script must dump the same lines.</p>
 */
public final class TableDump {

    private TableDump() {
    }

    /** Sorted, human-readable lines for one table. */
    public static List<String> lines(final TableId table) {
        final List<String> lines = new ArrayList<>();
        switch (table) {
            case SIEVE -> {
                for (final Map.Entry<Item, List<SieveDrop>> entry : SieveRegistry.all().entrySet()) {
                    final String input = id(entry.getKey());
                    for (final SieveDrop drop : entry.getValue()) {
                        lines.add("sieve " + input + " -> " + id(drop.item()) + " "
                                + Float.toString(drop.chance()) + " " + count(drop.min(), drop.max()));
                    }
                }
            }
            case HAMMER -> {
                for (final Map.Entry<Block, HammerRecipes.Rule> entry : HammerRecipes.all()) {
                    final HammerRecipes.Rule rule = entry.getValue();
                    lines.add("hammer " + id(entry.getKey()) + " -> " + id(rule.item()) + " "
                            + count(rule.min(), rule.max()));
                }
            }
            case CROOK -> {
                for (final Map.Entry<Block, List<CrookDrops.Drop>> entry : CrookDrops.all()) {
                    final String input = id(entry.getKey());
                    for (final CrookDrops.Drop drop : entry.getValue()) {
                        lines.add("crook " + input + " -> " + id(drop.item()) + " "
                                + Float.toString(drop.chance()));
                    }
                }
            }
            case BARREL -> {
                for (final BarrelRecipes.Recipe recipe : BarrelRecipes.all()) {
                    lines.add(barrel(recipe));
                }
            }
            case COMPOST -> {
                for (final CompostRegistry.Entry entry : CompostRegistry.all()) {
                    final String key = entry.item() != null
                            ? id(entry.item())
                            : "#" + entry.tag().location();
                    lines.add("compost " + key + " = " + entry.value());
                }
            }
            case CRUCIBLE_MELT -> {
                for (final CrucibleRecipes.Melt melt : CrucibleRecipes.all()) {
                    lines.add("crucible_melt " + id(melt.input()) + " -> "
                            + BuiltInRegistries.FLUID.getKey(melt.fluid()) + " "
                            + melt.amount() + " mB (solid " + melt.solidAmount() + ")");
                }
            }
            case CRUCIBLE_HEAT -> {
                for (final CrucibleHeat.HeatSource source : CrucibleHeat.all()) {
                    lines.add("crucible_heat " + id(source.block()) + properties(source.properties())
                            + " = " + Float.toString(source.speed()) + " mB/t");
                }
            }
        }
        lines.sort(String::compareTo);
        return List.copyOf(lines);
    }

    /**
     * Header line kept in the dump so a pasted log is self-describing.
     *
     * <p>It deliberately contains no overlay count: the same fixture expressed
     * as a datapack and as a script must produce identical output, and only one
     * of them has overlay operations. The count is reported by
     * {@code /exnihilo tables} instead.</p>
     */
    public static String header(final TableId table, final int lines) {
        return "# " + table.id() + " " + lines + " lines";
    }

    private static String barrel(final BarrelRecipes.Recipe recipe) {
        final StringBuilder line = new StringBuilder("barrel ");
        line.append(BuiltInRegistries.FLUID.getKey(recipe.base()));
        if (recipe.addedFluid() != null) {
            line.append(" + fluid:").append(BuiltInRegistries.FLUID.getKey(recipe.addedFluid()));
        } else {
            line.append(" + item:").append(id(recipe.addedItem()));
        }
        line.append(" -> ").append(id(recipe.result().getItem()));
        if (recipe.spawnsEntity()) {
            line.append(" entity:").append(recipe.entity());
            line.append(" cook:").append(recipe.cookTicks());
            final StringJoiner drops = new StringJoiner(",", "[", "]");
            for (final Item drop : recipe.peacefulDrops()) {
                drops.add(id(drop));
            }
            line.append(" peaceful:").append(drops);
        }
        return line.toString();
    }

    private static String properties(final Map<String, String> properties) {
        if (properties.isEmpty()) {
            return "";
        }
        final List<String> names = new ArrayList<>(properties.keySet());
        names.sort(String::compareTo);
        final StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (final String name : names) {
            joiner.add(name + "=" + properties.get(name));
        }
        return joiner.toString();
    }

    private static String count(final int min, final int max) {
        return min == max ? "x" + min : "x" + min + "-" + max;
    }

    private static String id(final Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static String id(final Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }
}
