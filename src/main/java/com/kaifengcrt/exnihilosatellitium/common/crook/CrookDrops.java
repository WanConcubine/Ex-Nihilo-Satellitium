package com.kaifengcrt.exnihilosatellitium.common.crook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extra leaf drops when a crook is used, loaded from
 * {@code data/<namespace>/crook/*.json}.
 *
 * <p>One file per leaves block: {@code input} is the leaves block and
 * {@code drops} is a list of item/chance entries. Vanilla leaf drops stay
 * untouched; the crook adds the listed entries on top.</p>
 *
 * <p>Inputs may be a leaves block id or a {@code #tag}; tag entries extend the
 * drops of every matching block (identical drops are kept once). A
 * {@code "patch": true} file is applied after all normal files and can append
 * drops or remove individual drops listed under {@code "remove"}.</p>
 *
 * <p>Tags are bound after the reload listeners run, so {@link #load} builds the
 * concrete part and {@link #rebuild(boolean)} runs again on
 * {@code TagsUpdatedEvent} to expand tags and apply patches.</p>
 */
public final class CrookDrops {

    public record Drop(Item item, float chance) {
    }

    private static final String DIR = "crook";
    private static volatile Map<Block, List<Drop>> table = Map.of();
    private static volatile Map<Block, List<Drop>> clientTable = Map.of();
    private static volatile List<DataPackJson.Source> sources = List.of();
    private static volatile List<DataPackJson.Source> clientSources = List.of();

    private CrookDrops() {
    }

    public static List<Drop> dropsFor(final Block block) {
        return table.getOrDefault(block, List.of());
    }

    /** JEI view, sorted by leaves block id for a stable page order. */
    public static List<Map.Entry<Block, List<Drop>>> all() {
        final Map<Block, List<Drop>> source = table.isEmpty() ? clientTable : table;
        final List<Map.Entry<Block, List<Drop>>> list = new ArrayList<>(source.entrySet());
        list.sort(Comparator.comparing(entry -> BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString()));
        return list;
    }

    public static void load(final ResourceManager manager) {
        load(manager, false);
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "crook");
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
        final Map<Block, List<Drop>> built = new HashMap<>();
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
                ExNihiloSatellitium.LOGGER.warn("[crook] missing input in {}", source.id());
                continue;
            }
            if (DataPackJson.isTag(rawInput)) {
                if (tagsReady) {
                    tagFiles.add(source);
                }
                continue;
            }
            final List<Block> inputs = blocks(rawInput, source.id());
            final List<Drop> drops = parseDrops(source.root(), source.id());
            for (final Block input : inputs) {
                if (built.putIfAbsent(input, drops) != null) {
                    ExNihiloSatellitium.LOGGER.warn(
                            "[crook] duplicate input {} in {}, later entry skipped",
                            BuiltInRegistries.BLOCK.getKey(input), source.id());
                }
            }
        }

        if (tagsReady) {
            for (final DataPackJson.Source source : tagFiles) {
                final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
                final List<Drop> drops = parseDrops(source.root(), source.id());
                if (drops.isEmpty()) {
                    continue;
                }
                for (final Block input : inputs) {
                    merge(built, input, drops);
                }
            }
            for (final DataPackJson.Source source : patchFiles) {
                applyPatch(built, source);
            }
        }

        final Map<Block, List<Drop>> snapshot = Map.copyOf(built);
        if (clientView) {
            clientTable = snapshot;
        } else {
            table = snapshot;
        }
        final int drops = built.values().stream().mapToInt(List::size).sum();
        ExNihiloSatellitium.LOGGER.info("[crook] loaded {} leaves tables, {} drops ({} tag files, {} patches{}){}",
                built.size(), drops, tagFiles.size(), patchFiles.size(),
                tagsReady ? "" : ", tags not ready", clientView ? " (client)" : "");
    }

    private static void applyPatch(final Map<Block, List<Drop>> built,
                                   final DataPackJson.Source source) {
        final JsonObject root = source.root();
        final List<Block> inputs = blocks(DataPackJson.string(root, "input"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        if (DataPackJson.removeAll(root)) {
            for (final Block input : inputs) {
                built.remove(input);
            }
            return;
        }

        final Set<Item> removed = new LinkedHashSet<>();
        for (final String raw : DataPackJson.removeList(root)) {
            removed.addAll(items(raw, source.id()));
        }
        final List<Drop> added = root.has("drops") ? parseDrops(root, source.id()) : List.of();
        if (removed.isEmpty() && added.isEmpty()) {
            ExNihiloSatellitium.LOGGER.warn("[crook] empty patch {} (no drops/remove)", source.id());
            return;
        }

        for (final Block input : inputs) {
            final List<Drop> list = new ArrayList<>(built.getOrDefault(input, List.of()));
            if (!removed.isEmpty()) {
                list.removeIf(drop -> removed.contains(drop.item()));
            }
            for (final Drop drop : added) {
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

    private static void merge(final Map<Block, List<Drop>> built, final Block input,
                              final List<Drop> drops) {
        final List<Drop> list = new ArrayList<>(built.getOrDefault(input, List.of()));
        for (final Drop drop : drops) {
            if (!list.contains(drop)) {
                list.add(drop);
            }
        }
        if (!list.isEmpty()) {
            built.put(input, List.copyOf(list));
        }
    }

    private static List<Block> blocks(final String raw, final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[crook] missing input in {}", file);
            return List.of();
        }
        return DataPackJson.resolve(BuiltInRegistries.BLOCK, raw, "crook", file).values();
    }

    private static List<Item> items(final String raw, final ResourceLocation file) {
        final List<Item> resolved = DataPackJson.resolve(BuiltInRegistries.ITEM, raw, "crook", file).values();
        return resolved.stream().filter(item -> item != Items.AIR).toList();
    }

    private static List<Drop> parseDrops(final JsonObject root, final ResourceLocation file) {
        final JsonElement drops = root.get("drops");
        if (drops == null || !drops.isJsonArray()) {
            ExNihiloSatellitium.LOGGER.warn("[crook] {} has no drops array", file);
            return List.of();
        }
        final JsonArray array = drops.getAsJsonArray();
        final List<Drop> list = new ArrayList<>();
        for (final JsonElement element : array) {
            final JsonObject drop = element.getAsJsonObject();
            if (!DataPackJson.modLoaded(drop)) {
                continue;
            }
            final String rawItem = DataPackJson.string(drop, "item");
            if (rawItem == null) {
                ExNihiloSatellitium.LOGGER.warn("[crook] drop without item in {}", file);
                continue;
            }
            final ResourceLocation itemId = DataPackJson.id(rawItem, "crook", file);
            final Item item = itemId == null
                    ? null
                    : BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
            final float chance = drop.has("chance") ? drop.get("chance").getAsFloat() : Float.NaN;
            if (item == null || item == Items.AIR || !Float.isFinite(chance)
                    || chance <= 0.0F || chance > 1.0F) {
                ExNihiloSatellitium.LOGGER.warn("[crook] bad drop {} in {}", rawItem, file);
                continue;
            }
            list.add(new Drop(item, chance));
        }
        return List.copyOf(list);
    }
}
