package com.kaifengcrt.exnihilosatellitium.common.table;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Versioned, network-independent view of the effective tables (P4 S1).
 *
 * <p>The snapshot is the <em>merged</em> table in the same JSON shape as
 * {@code data/exnihilosatellitium/<table>/*.json}, so a client can rebuild it
 * through the existing parsers instead of growing a second deserializer. Tag
 * inputs are exported as-is; the client resolves them against its synced tags.</p>
 *
 * <p>{@link #checkCounts()} guards the export: every table's snapshot must
 * contain exactly as many records as {@link TableDump} renders lines for it.
 * That catches a forgotten branch in the switch below.</p>
 */
public final class TableSnapshot {

    /** Bump when the JSON shape changes; a client rejects a different value. */
    public static final int SCHEMA = 1;

    private final Map<TableId, List<JsonObject>> files;

    private TableSnapshot(final Map<TableId, List<JsonObject>> files) {
        this.files = files;
    }

    /** Exports every table from the live (server) registries. */
    public static TableSnapshot capture() {
        final Map<TableId, List<JsonObject>> out = new EnumMap<>(TableId.class);
        for (final TableId table : TableId.values()) {
            out.put(table, export(table));
        }
        return new TableSnapshot(out);
    }

    public Map<TableId, List<JsonObject>> files() {
        return this.files;
    }

    public int fileCount() {
        return this.files.values().stream().mapToInt(List::size).sum();
    }

    public JsonObject toJson() {
        final JsonObject root = new JsonObject();
        root.addProperty("schema", SCHEMA);
        final JsonObject tables = new JsonObject();
        for (final Map.Entry<TableId, List<JsonObject>> entry : this.files.entrySet()) {
            final JsonArray files = new JsonArray();
            entry.getValue().forEach(files::add);
            tables.add(entry.getKey().id(), files);
        }
        root.add("tables", tables);
        return root;
    }

    /** Rejects a snapshot with a different schema instead of applying garbage. */
    public static TableSnapshot fromJson(final JsonObject root) {
        final int schema = root.has("schema") ? root.get("schema").getAsInt() : -1;
        if (schema != SCHEMA) {
            throw new IllegalArgumentException("snapshot schema " + schema + " != " + SCHEMA);
        }
        final Map<TableId, List<JsonObject>> out = new EnumMap<>(TableId.class);
        final JsonObject tables = root.has("tables") ? root.getAsJsonObject("tables") : new JsonObject();
        for (final TableId table : TableId.values()) {
            final List<JsonObject> list = new ArrayList<>();
            if (tables.has(table.id())) {
                tables.getAsJsonArray(table.id()).forEach(element -> list.add(element.getAsJsonObject()));
            }
            out.put(table, List.copyOf(list));
        }
        return new TableSnapshot(out);
    }

    /**
     * Compares the exported record count of every table with the number of dump
     * lines the same table produces. Returns one message per mismatch.
     */
    public List<String> checkCounts() {
        final List<String> problems = new ArrayList<>();
        for (final TableId table : TableId.values()) {
            final int exported = records(table, this.files.getOrDefault(table, List.of()));
            final int expected = TableDump.lines(table).size();
            if (exported != expected) {
                problems.add(table.id() + ": exported " + exported + " record(s), dump has " + expected);
            }
        }
        return problems;
    }

    private static int records(final TableId table, final List<JsonObject> files) {
        return switch (table) {
            case SIEVE -> files.stream()
                    .mapToInt(file -> file.getAsJsonArray("outputs").size())
                    .sum();
            case CROOK -> files.stream()
                    .mapToInt(file -> file.getAsJsonArray("drops").size())
                    .sum();
            case COMPOST -> files.stream()
                    .mapToInt(file -> file.getAsJsonObject("values").size())
                    .sum();
            default -> files.size();
        };
    }

    private static List<JsonObject> export(final TableId table) {
        final List<JsonObject> out = new ArrayList<>();
        switch (table) {
            case SIEVE -> {
                for (final Map.Entry<Item, List<SieveDrop>> entry : SieveRegistry.all().entrySet()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("input", id(entry.getKey()));
                    final JsonArray drops = new JsonArray();
                    for (final SieveDrop drop : entry.getValue()) {
                        final JsonObject dropJson = new JsonObject();
                        dropJson.addProperty("item", id(drop.item()));
                        dropJson.addProperty("chance", drop.chance());
                        if (drop.min() != 1 || drop.max() != 1) {
                            dropJson.addProperty("min", drop.min());
                            dropJson.addProperty("max", drop.max());
                        }
                        drops.add(dropJson);
                    }
                    file.add("outputs", drops);
                    out.add(file);
                }
            }
            case HAMMER -> {
                for (final Map.Entry<Block, HammerRecipes.Rule> entry : HammerRecipes.all()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("input", id(entry.getKey()));
                    file.addProperty("result", id(entry.getValue().item()));
                    file.addProperty("min", entry.getValue().min());
                    file.addProperty("max", entry.getValue().max());
                    out.add(file);
                }
            }
            case CROOK -> {
                for (final Map.Entry<Block, List<CrookDrops.Drop>> entry : CrookDrops.all()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("input", id(entry.getKey()));
                    final JsonArray drops = new JsonArray();
                    for (final CrookDrops.Drop drop : entry.getValue()) {
                        final JsonObject dropJson = new JsonObject();
                        dropJson.addProperty("item", id(drop.item()));
                        dropJson.addProperty("chance", drop.chance());
                        drops.add(dropJson);
                    }
                    file.add("drops", drops);
                    out.add(file);
                }
            }
            case COMPOST -> {
                final JsonObject file = new JsonObject();
                final JsonObject values = new JsonObject();
                for (final CompostRegistry.Entry entry : CompostRegistry.all()) {
                    final String key = entry.item() != null
                            ? id(entry.item())
                            : "#" + entry.tag().location();
                    values.addProperty(key, entry.value());
                }
                file.add("values", values);
                out.add(file);
            }
            case BARREL -> {
                for (final BarrelRecipes.Recipe recipe : BarrelRecipes.all()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("base", fluidId(recipe.base()));
                    final JsonObject added = new JsonObject();
                    if (recipe.addedFluid() != null) {
                        added.addProperty("fluid", fluidId(recipe.addedFluid()));
                    } else {
                        added.addProperty("item", id(recipe.addedItem()));
                    }
                    file.add("added", added);
                    if (recipe.resultFluid() != null) {
                        file.addProperty("result_fluid", fluidId(recipe.resultFluid()));
                    } else if (recipe.entity() != null) {
                        file.addProperty("entity", recipe.entity().toString());
                        final JsonArray drops = new JsonArray();
                        for (final Item item : recipe.peacefulDrops()) {
                            drops.add(id(item));
                        }
                        file.add("peaceful_drop", drops);
                    } else {
                        file.addProperty("result", id(recipe.result().getItem()));
                    }
                    if (recipe.cookTicks() > 0) {
                        file.addProperty("cook_ticks", recipe.cookTicks());
                    }
                    if (recipe.delayTicks() > 0) {
                        file.addProperty("delay_ticks", recipe.delayTicks());
                    }
                    if (recipe.drain() > 0) {
                        file.addProperty("drain", recipe.drain());
                    }
                    out.add(file);
                }
            }
            case CRUCIBLE_MELT -> {
                for (final CrucibleRecipes.Melt melt : CrucibleRecipes.all()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("input", id(melt.input()));
                    file.addProperty("fluid", fluidId(melt.fluid()));
                    file.addProperty("amount", melt.amount());
                    if (melt.solidAmount() > 0) {
                        file.addProperty("solid", melt.solidAmount());
                    }
                    out.add(file);
                }
            }
            case CRUCIBLE_HEAT -> {
                for (final CrucibleHeat.HeatSource source : CrucibleHeat.all()) {
                    final JsonObject file = new JsonObject();
                    file.addProperty("block", id(source.block()));
                    final JsonObject properties = new JsonObject();
                    source.properties().forEach(properties::addProperty);
                    file.add("properties", properties);
                    file.addProperty("speed", source.speed());
                    out.add(file);
                }
            }
        }
        return List.copyOf(out);
    }

    private static String id(final Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static String id(final Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static String fluidId(final net.minecraft.world.level.material.Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid).toString();
    }
}
