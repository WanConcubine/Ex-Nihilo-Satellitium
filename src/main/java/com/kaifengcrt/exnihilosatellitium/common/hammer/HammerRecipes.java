package com.kaifengcrt.exnihilosatellitium.common.hammer;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableBuilder;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Hammer crush table, loaded from {@code data/<namespace>/hammer/*.json}.
 *
 * <p>Each file maps one block to the item stack it drops when hit by a hammer,
 * plus an optional drop-count range ({@code min} / {@code max}, inclusive,
 * default 1):</p>
 *
 * <pre>
 * cobblestone   → 1 gravel / sand / dust
 * ore gravel    → 4-6 crushed pieces
 * ore sand      → 4-6 powdered pieces
 * ore dust      → the ore dust block itself (no further crushing)
 * </pre>
 *
 * <p>One rule per block, so a {@code #tag} input expands to every matching
 * block but never overwrites a block that already has a concrete rule. Use
 * {@code "patch": true} to replace a rule and {@code "remove": true} to delete
 * one. Gameplay always reads the server table; the client keeps a copy loaded
 * from the mirrored assets so JEI works when connected to a dedicated server.</p>
 *
 * <p>Tags are bound after the reload listeners run, so {@link #load} builds the
 * concrete part and {@link #rebuild(boolean)} runs again on
 * {@code TagsUpdatedEvent} to expand tags and apply patches. Script
 * contributions from {@link CustomizationOverlay} are replayed last.</p>
 */
public final class HammerRecipes {

    private static final String DIR = "hammer";
    private static final int MAX_COUNT = 64;

    private static volatile Map<Block, Rule> recipes = Map.of();
    private static volatile Map<Block, Rule> clientRecipes = Map.of();
    private static volatile List<DataPackJson.Source> sources = List.of();
    private static volatile List<DataPackJson.Source> clientSources = List.of();

    /** One hammer rule: the dropped item and the inclusive drop-count range. */
    public record Rule(Item item, int min, int max) {

        public ItemStack roll(final RandomSource random) {
            final int count = min >= max ? min : min + random.nextInt(max - min + 1);
            return new ItemStack(item, count);
        }
    }

    private HammerRecipes() {
    }

    /** The rule for this block, or {@code null} when no hammer rule matches. */
    public static Rule ruleFor(final BlockState state) {
        return recipes.get(state.getBlock());
    }

    /** JEI view, sorted by block id for a stable page order. */
    public static List<Map.Entry<Block, Rule>> all() {
        final Map<Block, Rule> source = recipes.isEmpty() ? clientRecipes : recipes;
        final List<Map.Entry<Block, Rule>> list = new ArrayList<>(source.entrySet());
        list.sort(Comparator.comparing(entry -> BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString()));
        return list;
    }

    public static void load(final ResourceManager manager) {
        load(manager, false);
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "hammer");
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
        final TableBuilder<Block, Rule> builder = new TableBuilder<>(TableId.HAMMER.id(),
                block -> BuiltInRegistries.BLOCK.getKey(block).toString());
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
                    ExNihiloSatellitium.LOGGER.warn("[hammer] missing input in {}", source.id());
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
            ExNihiloSatellitium.LOGGER.error("[hammer] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final Map<Block, Rule> snapshot = builder.build();
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
            builder.logConflicts();
        }
        ExNihiloSatellitium.LOGGER.info(
                "[hammer] loaded {} recipes ({} tag files, {} patches, {} overlay{}){}",
                snapshot.size(), tagFiles.size(), patchFiles.size(),
                CustomizationOverlay.forTable(TableId.HAMMER).size(),
                tagsReady ? "" : ", waiting for tags", clientView ? " (client)" : "");
    }

    private static void applyFile(final TableBuilder<Block, Rule> builder,
                                  final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        final Rule rule = parseRule(source.root(), source.id());
        if (rule == null) {
            return;
        }
        for (final Block input : inputs) {
            if (!builder.putIfAbsent(input, rule, source.id().toString())) {
                ExNihiloSatellitium.LOGGER.warn(
                        "[hammer] duplicate input {} in {}, later entry skipped",
                        BuiltInRegistries.BLOCK.getKey(input), source.id());
            }
        }
    }

    private static void applyTagFile(final TableBuilder<Block, Rule> builder,
                                     final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        final Rule rule = parseRule(source.root(), source.id());
        if (rule == null) {
            return;
        }
        for (final Block input : inputs) {
            builder.putIfAbsent(input, rule, source.id().toString());
        }
    }

    private static void applyPatch(final TableBuilder<Block, Rule> builder,
                                   final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "input"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        if (DataPackJson.removeAll(source.root())) {
            for (final Block input : inputs) {
                builder.remove(input);
            }
            return;
        }
        final Rule rule = parseRule(source.root(), source.id());
        if (rule == null) {
            return;
        }
        for (final Block input : inputs) {
            builder.put(input, rule, source.id().toString());
        }
    }

    private static void applyOverlay(final TableBuilder<Block, Rule> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.HAMMER)) {
            try {
                final ResourceLocation label = overlayLabel(customization);
                final JsonObject root = customization.payload();
                final List<Block> inputs = blocks(DataPackJson.string(root, "input"), label);
                if (inputs.isEmpty()) {
                    continue;
                }
                if (customization.action() == CustomizationOverlay.Action.REMOVE) {
                    for (final Block input : inputs) {
                        builder.remove(input, overlaySource(customization));
                    }
                    continue;
                }
                final Rule rule = parseRule(root, label);
                if (rule == null) {
                    continue;
                }
                final String source = overlaySource(customization);
                if (customization.action() == CustomizationOverlay.Action.ADD) {
                    for (final Block input : inputs) {
                        builder.putIfAbsent(input, rule, source);
                    }
                } else {
                    for (final Block input : inputs) {
                        builder.put(input, rule, source);
                    }
                }
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[hammer] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static ResourceLocation overlayLabel(final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = ResourceLocation.tryParse(
                "customize:hammer/" + customization.sequence());
        return label == null ? ResourceLocation.tryParse("customize:hammer/0") : label;
    }

    private static String overlaySource(final CustomizationOverlay.Customization customization) {
        return "customize:" + customization.source() + "#" + customization.sequence();
    }

    private static List<Block> blocks(final String raw, final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[hammer] missing input in {}", file);
            return List.of();
        }
        return DataPackJson.resolve(BuiltInRegistries.BLOCK, raw, "hammer", file).values();
    }

    private static Rule parseRule(final JsonObject root, final ResourceLocation file) {
        final String rawResult = DataPackJson.string(root, "result");
        if (rawResult == null) {
            ExNihiloSatellitium.LOGGER.warn("[hammer] {} has no result", file);
            return null;
        }
        final ResourceLocation resultId = DataPackJson.id(rawResult, "hammer", file);
        final Item result = resultId == null
                ? null
                : BuiltInRegistries.ITEM.getOptional(resultId).orElse(null);
        if (result == null || result == Items.AIR) {
            ExNihiloSatellitium.LOGGER.warn("[hammer] unknown result {} in {}", rawResult, file);
            return null;
        }
        final int min = root.has("min") ? root.get("min").getAsInt() : 1;
        final int max = root.has("max") ? root.get("max").getAsInt() : min;
        if (min < 1 || max < min || max > MAX_COUNT) {
            ExNihiloSatellitium.LOGGER.warn("[hammer] invalid count range {}..{} in {}, skipped",
                    min, max, file);
            return null;
        }
        return new Rule(result, min, max);
    }
}
