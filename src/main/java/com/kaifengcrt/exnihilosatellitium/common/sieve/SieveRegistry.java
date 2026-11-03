package com.kaifengcrt.exnihilosatellitium.common.sieve;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableBuilder;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
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
 * again from {@code TagsUpdatedEvent} to expand tags and apply patches. Script
 * contributions from {@link CustomizationOverlay} are replayed after the
 * patches, on the same builder.</p>
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
        // Concrete entries are available immediately; tags are expanded on TagsUpdatedEvent.
        // The client keeps the old bindings across F3+T, so it can expand right away.
        rebuild(clientView, clientView);
    }

    /** Rebuilds from the last read sources; called again after tags are bound. */
    public static void rebuild(final boolean clientView) {
        rebuild(clientView, true);
    }

    private static void rebuild(final boolean clientView, final boolean expandTags) {
        final List<DataPackJson.Source> sourceList = clientView ? clientSources : sources;
        final boolean tagsReady = expandTags && DataPackJson.tagsReady();
        final TableBuilder<Item, List<SieveDrop>> builder = new TableBuilder<>(TableId.SIEVE.id());
        final List<DataPackJson.Source> tagFiles = new ArrayList<>();
        final List<DataPackJson.Source> patchFiles = new ArrayList<>();

        try {
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
                applyFile(builder, source);
            }

            if (tagsReady) {
                for (final DataPackJson.Source source : tagFiles) {
                    applyTagFile(builder, source);
                }
                for (final DataPackJson.Source source : patchFiles) {
                    applyPatch(builder, source);
                }
                applyOverlay(builder);
            }
        } catch (final RuntimeException exception) {
            // Keep the previous snapshot instead of publishing a half-built table.
            ExNihiloSatellitium.LOGGER.error("[sieve] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final Map<Item, List<SieveDrop>> snapshot = builder.build();
        if (clientView) {
            clientTable = snapshot;
        } else {
            table = snapshot;
        }
        final int drops = snapshot.values().stream().mapToInt(List::size).sum();
        ExNihiloSatellitium.LOGGER.info(
                "[sieve] loaded {} inputs, {} drops ({} tag files, {} patches, {} overlay{}){}",
                snapshot.size(), drops, tagFiles.size(), patchFiles.size(),
                CustomizationOverlay.forTable(TableId.SIEVE).size(),
                tagsReady ? "" : ", waiting for tags", clientView ? " (client)" : "");
    }

    private static void applyFile(final TableBuilder<Item, List<SieveDrop>> builder,
                                  final DataPackJson.Source source) {
        final List<Item> inputs = items(DataPackJson.string(source.root(), "input"), source.id());
        final List<SieveDrop> drops = parseDrops(source.root(), source.id());
        if (inputs.isEmpty() || drops.isEmpty()) {
            return;
        }
        for (final Item input : inputs) {
            if (!builder.put(input, drops, source.id().toString())) {
                ExNihiloSatellitium.LOGGER.warn(
                        "[sieve] duplicate input {} in {}, overwriting",
                        BuiltInRegistries.ITEM.getKey(input), source.id());
            }
        }
    }

    private static void applyTagFile(final TableBuilder<Item, List<SieveDrop>> builder,
                                     final DataPackJson.Source source) {
        final List<Item> inputs = items(DataPackJson.string(source.root(), "input"), source.id());
        final List<SieveDrop> drops = parseDrops(source.root(), source.id());
        if (inputs.isEmpty() || drops.isEmpty()) {
            return;
        }
        for (final Item input : inputs) {
            builder.merge(input, drops, SieveRegistry::union, source.id().toString());
        }
    }

    private static void applyPatch(final TableBuilder<Item, List<SieveDrop>> builder,
                                   final DataPackJson.Source source) {
        final JsonObject root = source.root();
        final List<Item> inputs = items(DataPackJson.string(root, "input"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        if (DataPackJson.removeAll(root)) {
            for (final Item input : inputs) {
                builder.remove(input);
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
            final List<SieveDrop> list = new ArrayList<>(builder.get(input) == null
                    ? List.of() : builder.get(input));
            if (!removed.isEmpty()) {
                list.removeIf(drop -> removed.contains(drop.item()));
            }
            for (final SieveDrop drop : added) {
                if (!list.contains(drop)) {
                    list.add(drop);
                }
            }
            if (list.isEmpty()) {
                builder.remove(input);
            } else {
                builder.put(input, List.copyOf(list), source.id().toString());
            }
        }
    }

    /** Replays queued script operations after the datapack files and patches. */
    private static void applyOverlay(final TableBuilder<Item, List<SieveDrop>> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.SIEVE)) {
            try {
                switch (customization.action()) {
                    case ADD -> overlayAdd(builder, customization);
                    case REPLACE -> overlayReplace(builder, customization);
                    case REMOVE -> overlayRemove(builder, customization);
                }
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[sieve] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static void overlayAdd(final TableBuilder<Item, List<SieveDrop>> builder,
                                   final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = overlayLabel(customization);
        final JsonObject root = customization.payload();
        final List<Item> inputs = items(DataPackJson.string(root, "input"), label);
        final List<SieveDrop> drops = parseDrops(root, label);
        if (inputs.isEmpty() || drops.isEmpty()) {
            return;
        }
        for (final Item input : inputs) {
            builder.merge(input, drops, SieveRegistry::union, overlaySource(customization));
        }
    }

    private static void overlayReplace(final TableBuilder<Item, List<SieveDrop>> builder,
                                       final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = overlayLabel(customization);
        final JsonObject root = customization.payload();
        final List<Item> inputs = items(DataPackJson.string(root, "input"), label);
        if (!root.has("outputs")) {
            ExNihiloSatellitium.LOGGER.warn("[sieve] overlay {} has no outputs", customization.describe());
            return;
        }
        final List<SieveDrop> drops = parseDrops(root, label);
        if (drops.isEmpty() && !isEmptyArray(root, "outputs")) {
            ExNihiloSatellitium.LOGGER.warn(
                    "[sieve] overlay {} has no valid outputs, keeping the old entry",
                    customization.describe());
            return;
        }
        for (final Item input : inputs) {
            if (drops.isEmpty()) {
                builder.remove(input);
            } else {
                builder.put(input, drops, overlaySource(customization));
            }
        }
    }

    private static void overlayRemove(final TableBuilder<Item, List<SieveDrop>> builder,
                                      final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = overlayLabel(customization);
        final JsonObject root = customization.payload();
        final List<Item> inputs = items(DataPackJson.string(root, "input"), label);
        if (inputs.isEmpty()) {
            return;
        }
        final List<String> remove = DataPackJson.removeList(root);
        if (remove.isEmpty()) {
            for (final Item input : inputs) {
                builder.remove(input);
            }
            return;
        }
        final Set<Item> removed = new LinkedHashSet<>();
        for (final String raw : remove) {
            removed.addAll(items(raw, label));
        }
        for (final Item input : inputs) {
            final List<SieveDrop> current = builder.get(input);
            if (current == null) {
                continue;
            }
            final List<SieveDrop> list = new ArrayList<>(current);
            list.removeIf(drop -> removed.contains(drop.item()));
            if (list.isEmpty()) {
                builder.remove(input);
            } else {
                builder.put(input, List.copyOf(list), overlaySource(customization));
            }
        }
    }

    private static ResourceLocation overlayLabel(final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = ResourceLocation.tryParse(
                "exnihilosatellitium:overlay/" + customization.sequence());
        return label == null ? ResourceLocation.tryParse("exnihilosatellitium:overlay/0") : label;
    }

    private static String overlaySource(final CustomizationOverlay.Customization customization) {
        return "customize:" + customization.source() + "#" + customization.sequence();
    }

    /** {@code true} only for an explicit empty array; missing/invalid arrays are handled earlier. */
    private static boolean isEmptyArray(final JsonObject root, final String key) {
        return root.has(key) && root.get(key).isJsonArray() && root.getAsJsonArray(key).isEmpty();
    }

    private static List<SieveDrop> union(final List<SieveDrop> left, final List<SieveDrop> right) {
        final List<SieveDrop> list = new ArrayList<>(left);
        for (final SieveDrop drop : right) {
            if (!list.contains(drop)) {
                list.add(drop);
            }
        }
        return List.copyOf(list);
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
