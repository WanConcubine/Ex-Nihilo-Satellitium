package com.kaifengcrt.exnihilosatellitium.common.sieve;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sift table, loaded from {@code data/<namespace>/sieve/*.json}.
 *
 * <p>Each file describes one input and a list of drops; every drop is rolled
 * independently ({@code chance} per sift). The mesh tier does not change the
 * table, it only decides how many sifts a mesh survives.</p>
 *
 * <p>Inputs may be an item id or an item tag ({@code "#c:gravels"}). Tag entries
 * extend the drops of every matching item; a concrete entry and a tag entry for
 * the same item are merged, and identical drops are kept once. A file with
 * {@code "patch": true} is applied after all normal files; it can append drops
 * and remove individual drops listed under {@code "remove"}.</p>
 *
 * <p>Tags are bound only after every resource-reload listener has run, so
 * {@link #load} builds the concrete part and {@link #rebuild(boolean)} is called
 * again from {@code TagsUpdatedEvent} to expand tags and apply patches.</p>
 */
public final class SieveRegistry {

    private static final String DIR = "sieve";
    /** Gameplay table (server); replaced wholesale on every reload. */
    private static volatile Map<Item, List<SieveDrop>> table = Map.of();
    /** Client-side copy for JEI; never used for server-side sifting. */
    private static volatile Map<Item, List<SieveDrop>> clientTable = Map.of();
    /** Raw sources kept so the table can be rebuilt once tags are bound. */
    private static volatile List<DataPackJson.Source> sources = List.of();
    private static volatile List<DataPackJson.Source> clientSources = List.of();

    private SieveRegistry() {
    }

    public static List<SieveDrop> dropsFor(final ItemStack stack) {
        return table.getOrDefault(stack.getItem(), List.of());
    }

    /** Immutable snapshot for recipe browsers (JEI); key order is not meaningful. */
    public static Map<Item, List<SieveDrop>> all() {
        return table.isEmpty() ? clientTable : table;
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "sieve");
        if (clientView) {
            clientSources = read;
        } else {
            sources = read;
        }
        rebuild(clientView);
    }

    /** Rebuilds from the last read sources; called again after tags are bound. */
    public static void rebuild(final boolean clientView) {
        final List<DataPackJson.Source> sourceList = clientView ? clientSources : sources;
        final boolean tagsReady = DataPackJson.tagsReady();
        final Map<Item, List<SieveDrop>> built = new HashMap<>();
        final List<DataPackJson.Source> tagFiles = new ArrayList<>();
        final List<DataPackJson.Source> patchFiles = new ArrayList<>();

        for (final DataPackJson.Source source : sourceList) {
            if (!DataPackJson.modLoaded(source.root())) {
                continue;
            }
            if (DataPackJson.isPatch(source.root())) {
                patchFiles.add(source);
                continue;
            }
            final String rawInput = DataPackJson.string(source.root(), "input");
            if (rawInput == null) {
                ExNihiloSatellitium.LOGGER.warn("[sieve] missing input in {}", source.id());
                continue;
            }
            if (DataPackJson.isTag(rawInput)) {
                if (tagsReady) {
                    tagFiles.add(source);
                }
                continue;
            }
            final List<Item> inputs = items(rawInput, source.id());
            if (inputs.isEmpty()) {
                continue;
            }
            final List<SieveDrop> drops = parseDrops(source.root(), source.id());
            for (final Item input : inputs) {
                if (built.containsKey(input)) {
                    ExNihiloSatellitium.LOGGER.warn(
                            "[sieve] duplicate input {} in {}, overwriting",
                            BuiltInRegistries.ITEM.getKey(input), source.id());
                }
                built.put(input, drops);
            }
        }

        if (tagsReady) {
            for (final DataPackJson.Source source : tagFiles) {
                final List<Item> inputs = items(DataPackJson.string(source.root(), "input"), source.id());
                final List<SieveDrop> drops = parseDrops(source.root(), source.id());
                if (drops.isEmpty()) {
                    continue;
                }
                for (final Item input : inputs) {
                    merge(built, input, drops);
                }
            }
            for (final DataPackJson.Source source : patchFiles) {
                applyPatch(built, source);
            }
        }

        final Map<Item, List<SieveDrop>> snapshot = Map.copyOf(built);
        if (clientView) {
            clientTable = snapshot;
        } else {
            table = snapshot;
        }
        final int drops = built.values().stream().mapToInt(List::size).sum();
        ExNihiloSatellitium.LOGGER.info("[sieve] loaded {} inputs, {} drops ({} tag files, {} patches{}){}",
                built.size(), drops, tagFiles.size(), patchFiles.size(),
                tagsReady ? "" : ", tags not ready", clientView ? " (client)" : "");
    }

    private static void applyPatch(final Map<Item, List<SieveDrop>> built,
                                   final DataPackJson.Source source) {
        final JsonObject root = source.root();
        final List<Item> inputs = items(DataPackJson.string(root, "input"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        if (DataPackJson.removeAll(root)) {
            for (final Item input : inputs) {
                built.remove(input);
            }
            return;
        }

        final Set<Item> removed = new LinkedHashSet<>();
        for (final String raw : DataPackJson.removeList(root)) {
            removed.addAll(items(raw, source.id()));
        }
        final List<SieveDrop> added = root.has("outputs")
                ? parseDrops(root, source.id())
                : List.of();
        if (removed.isEmpty() && added.isEmpty()) {
            ExNihiloSatellitium.LOGGER.warn("[sieve] empty patch {} (no outputs/remove)", source.id());
            return;
        }

        for (final Item input : inputs) {
            final List<SieveDrop> list = new ArrayList<>(built.getOrDefault(input, List.of()));
            if (!removed.isEmpty()) {
                list.removeIf(drop -> removed.contains(drop.item()));
            }
            for (final SieveDrop drop : added) {
                if (!list.contains(drop)) {
                    list.add(drop);
                }
            }
            if (list.isEmpty()) {
                built.remove(input);
            } else {
                built.put(input, List.copyOf(list));
            }
        }
    }

    private static void merge(final Map<Item, List<SieveDrop>> built, final Item input,
                              final List<SieveDrop> drops) {
        final List<SieveDrop> list = new ArrayList<>(built.getOrDefault(input, List.of()));
        for (final SieveDrop drop : drops) {
            if (!list.contains(drop)) {
                list.add(drop);
            }
        }
        if (!list.isEmpty()) {
            built.put(input, List.copyOf(list));
        }
    }

    private static List<Item> items(final String raw, final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[sieve] missing input in {}", file);
            return List.of();
        }
        final List<Item> resolved = DataPackJson.resolve(BuiltInRegistries.ITEM, raw, "sieve", file).values();
        return resolved.stream().filter(item -> item != Items.AIR).toList();
    }

    private static List<SieveDrop> parseDrops(final JsonObject root, final ResourceLocation file) {
        final JsonElement outputs = root.get("outputs");
        if (outputs == null || !outputs.isJsonArray()) {
            ExNihiloSatellitium.LOGGER.warn("[sieve] {} has no outputs array", file);
            return List.of();
        }
        final JsonArray array = outputs.getAsJsonArray();
        final List<SieveDrop> list = new ArrayList<>();
        for (final JsonElement element : array) {
            final JsonObject drop = element.getAsJsonObject();
            // Optional gate for soft-dependency drops (e.g. Tinkers seeds):
            // skip before touching the item registry, so an absent mod is silent.
            if (!DataPackJson.modLoaded(drop)) {
                continue;
            }
            final String rawItem = DataPackJson.string(drop, "item");
            if (rawItem == null) {
                ExNihiloSatellitium.LOGGER.warn("[sieve] drop without item in {}", file);
                continue;
            }
            final ResourceLocation itemId = DataPackJson.id(rawItem, "sieve", file);
            final Item item = itemId == null ? null : BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
            final float chance = drop.has("chance") ? drop.get("chance").getAsFloat() : Float.NaN;
            final int min = drop.has("min") ? drop.get("min").getAsInt() : 1;
            final int max = drop.has("max") ? drop.get("max").getAsInt() : min;
            if (item == null || item == Items.AIR || !Float.isFinite(chance)
                    || chance <= 0.0F || chance > 1.0F) {
                ExNihiloSatellitium.LOGGER.warn("[sieve] bad drop {} in {}", rawItem, file);
                continue;
            }
            if (min < 1 || max < min) {
                ExNihiloSatellitium.LOGGER.warn("[sieve] bad count {}..{} for {} in {}",
                        min, max, rawItem, file);
                continue;
            }
            list.add(new SieveDrop(item, chance, min, max));
        }
        return List.copyOf(list);
    }
}
