package com.kaifengcrt.exnihilosatellitium.common.crucible;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heat sources below a crucible, loaded from {@code data/<namespace>/crucible_heat/*.json}:
 *
 * <pre>
 * { "block": "minecraft:torch", "speed": 0.1 }
 * { "block": "minecraft:furnace", "properties": { "lit": "true" }, "speed": 0.15 }
 * </pre>
 *
 * <p>{@code speed} is mB melted per tick. All properties listed must match the
 * actual block state, so lit and unlit campfires/furnaces can differ.</p>
 *
 * <p>A {@code #tag} block input expands to every matching block. A patch file
 * can delete rules ({@code "remove": true}); when the patch also lists
 * {@code properties}, only matching rules are removed, otherwise every rule for
 * the block is. Tags are bound after the reload listeners run, so
 * {@link #load} builds the concrete part and {@link #rebuild(boolean)} runs
 * again on {@code TagsUpdatedEvent}. Script contributions from
 * {@link CustomizationOverlay} are replayed last.</p>
 */
public final class CrucibleHeat {

    private static final String DIR = "crucible_heat";
    private static final float MAX_SPEED = 4.0F;

    public record HeatSource(Block block, Map<String, String> properties, float speed) {

        /** Item used to show this source in JEI; fire/lava have no usable block item. */
        public ItemStack icon() {
            final Item item = this.block.asItem();
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
            if (this.block == Blocks.FIRE || this.block == Blocks.SOUL_FIRE) {
                return new ItemStack(Items.FLINT_AND_STEEL);
            }
            if (this.block == Blocks.LAVA) {
                return new ItemStack(Items.LAVA_BUCKET);
            }
            return new ItemStack(Items.BARRIER);
        }

        public boolean matches(final BlockState state) {
            if (state.getBlock() != this.block) {
                return false;
            }
            for (final Map.Entry<String, String> entry : this.properties.entrySet()) {
                final Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
                if (property == null) {
                    return false;
                }
                if (!String.valueOf(state.getValue(property)).equalsIgnoreCase(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
    }

    private static volatile List<HeatSource> sources = List.of();
    private static volatile List<HeatSource> clientSources = List.of();
    private static volatile List<DataPackJson.Source> files = List.of();
    private static volatile List<DataPackJson.Source> clientFiles = List.of();

    private CrucibleHeat() {
    }

    /**
     * mB/tick produced by the block state below the crucible; 0 when it is not a
     * heat source. Always reads the server table, so a client reload cannot
     * change melting behaviour.
     */
    public static float speedFor(final BlockState state) {
        for (final HeatSource source : sources) {
            if (source.matches(state)) {
                return source.speed();
            }
        }
        return 0.0F;
    }

    /** Client/Jade view: same match but against the client copy when present. */
    public static float speedForClient(final BlockState state) {
        for (final HeatSource source : all()) {
            if (source.matches(state)) {
                return source.speed();
            }
        }
        return 0.0F;
    }

    /** JEI view: client copy when present, otherwise the server table. */
    public static List<HeatSource> all() {
        return sources.isEmpty() ? clientSources : sources;
    }

    public static void load(final ResourceManager manager, final boolean clientView) {
        final List<DataPackJson.Source> read = DataPackJson.readAll(manager, DIR, "crucible");
        if (clientView) {
            clientFiles = read;
        } else {
            files = read;
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
        final List<DataPackJson.Source> sourceList = clientView ? clientFiles : files;
        final boolean tagsReady = expandTags && DataPackJson.tagsReady();
        final TableBuilder<String, HeatSource> builder = new TableBuilder<>(TableId.CRUCIBLE_HEAT.id());
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
                final String rawBlock = DataPackJson.string(source.root(), "block");
                if (rawBlock == null) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] missing block in {}", source.id());
                    continue;
                }
                if (DataPackJson.isTag(rawBlock)) {
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
            ExNihiloSatellitium.LOGGER.error("[crucible] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final List<HeatSource> snapshot = List.copyOf(builder.build().values());
        if (clientView) {
            clientSources = snapshot;
        } else {
            sources = snapshot;
            builder.logConflicts();
        }
        ExNihiloSatellitium.LOGGER.info(
                "[crucible] loaded {} heat sources ({} tag files, {} patches, {} overlay{}){}",
                snapshot.size(), tagFiles.size(), patchFiles.size(),
                CustomizationOverlay.forTable(TableId.CRUCIBLE_HEAT).size(),
                tagsReady ? "" : ", waiting for tags", clientView ? " (client)" : "");
    }

    private static void applyFile(final TableBuilder<String, HeatSource> builder,
                                  final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "block"), source.id());
        final Map<String, String> properties = properties(source.root());
        final float speed = speed(source.root(), source.id());
        if (inputs.isEmpty() || speed <= 0.0F) {
            return;
        }
        for (final Block input : inputs) {
            builder.putIfAbsent(key(input, properties),
                    new HeatSource(input, properties, speed), source.id().toString());
        }
    }

    private static void applyTagFile(final TableBuilder<String, HeatSource> builder,
                                     final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "block"), source.id());
        final Map<String, String> properties = properties(source.root());
        final float speed = speed(source.root(), source.id());
        if (inputs.isEmpty() || speed <= 0.0F) {
            return;
        }
        for (final Block input : inputs) {
            builder.putIfAbsent(key(input, properties),
                    new HeatSource(input, properties, speed), source.id().toString());
        }
    }

    private static void applyPatch(final TableBuilder<String, HeatSource> builder,
                                   final DataPackJson.Source source) {
        final List<Block> inputs = blocks(DataPackJson.string(source.root(), "block"), source.id());
        if (inputs.isEmpty()) {
            return;
        }
        final Map<String, String> properties = properties(source.root());
        removeMatching(builder, inputs, properties, null);
        if (DataPackJson.removeAll(source.root())) {
            return;
        }
        final float speed = speed(source.root(), source.id());
        if (speed <= 0.0F) {
            return;
        }
        for (final Block input : inputs) {
            builder.put(key(input, properties), new HeatSource(input, properties, speed),
                    source.id().toString());
        }
    }

    private static void applyOverlay(final TableBuilder<String, HeatSource> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.CRUCIBLE_HEAT)) {
            try {
                final ResourceLocation label = ResourceLocation.tryParse(
                        "exnihilosatellitium:overlay/" + customization.sequence());
                final JsonObject root = customization.payload();
                final List<Block> inputs = blocks(DataPackJson.string(root, "block"), label);
                if (inputs.isEmpty()) {
                    continue;
                }
                final Map<String, String> properties = properties(root);
                switch (customization.action()) {
                    case REMOVE -> removeMatching(builder, inputs, properties,
                            overlaySource(customization));
                    case ADD -> {
                        final float speed = speed(root, label);
                        if (speed <= 0.0F) {
                            continue;
                        }
                        for (final Block input : inputs) {
                            builder.putIfAbsent(key(input, properties),
                                    new HeatSource(input, properties, speed),
                                    overlaySource(customization));
                        }
                    }
                    case REPLACE -> {
                        final float speed = speed(root, label);
                        if (speed <= 0.0F) {
                            continue;
                        }
                        removeMatching(builder, inputs, properties, overlaySource(customization));
                        for (final Block input : inputs) {
                            builder.put(key(input, properties),
                                    new HeatSource(input, properties, speed),
                                    overlaySource(customization));
                        }
                    }
                }
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[crucible] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static void removeMatching(final TableBuilder<String, HeatSource> builder,
                                       final List<Block> inputs, final Map<String, String> properties,
                                       final String source) {
        if (!properties.isEmpty()) {
            for (final Block input : inputs) {
                builder.remove(key(input, properties), source);
            }
            return;
        }
        // No properties in the payload: drop every rule for these blocks, whatever their state.
        final List<String> doomed = new ArrayList<>();
        for (final String key : builder.keys()) {
            for (final Block input : inputs) {
                final String blockId = BuiltInRegistries.BLOCK.getKey(input).toString();
                if (key.equals(blockId) || key.startsWith(blockId + "[")) {
                    doomed.add(key);
                    break;
                }
            }
        }
        for (final String key : doomed) {
            builder.remove(key, source);
        }
    }

    private static String overlaySource(final CustomizationOverlay.Customization customization) {
        return "customize:" + customization.source() + "#" + customization.sequence();
    }

    /** Canonical key: block id plus the property map in sorted order. */
    private static String key(final Block block, final Map<String, String> properties) {
        final StringBuilder key = new StringBuilder(BuiltInRegistries.BLOCK.getKey(block).toString());
        if (!properties.isEmpty()) {
            final List<String> names = new ArrayList<>(properties.keySet());
            names.sort(String::compareTo);
            key.append('[');
            for (final String name : names) {
                key.append(name).append('=').append(properties.get(name)).append(',');
            }
            key.setLength(key.length() - 1);
            key.append(']');
        }
        return key.toString();
    }

    private static List<Block> blocks(final String raw, final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] missing block in {}", file);
            return List.of();
        }
        return DataPackJson.resolve(BuiltInRegistries.BLOCK, raw, "crucible", file).values();
    }

    private static Map<String, String> properties(final JsonObject root) {
        final Map<String, String> properties = new HashMap<>();
        if (root.has("properties") && root.get("properties").isJsonObject()) {
            for (final Map.Entry<String, JsonElement> property
                    : root.getAsJsonObject("properties").entrySet()) {
                properties.put(property.getKey(), property.getValue().getAsString());
            }
        }
        return Map.copyOf(properties);
    }

    private static float speed(final JsonObject root, final ResourceLocation file) {
        if (!root.has("speed")) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] missing speed in {}", file);
            return 0.0F;
        }
        final float speed = root.get("speed").getAsFloat();
        if (speed <= 0.0F || speed > MAX_SPEED) {
            ExNihiloSatellitium.LOGGER.warn("[crucible] invalid heat speed {} in {}", speed, file);
            return 0.0F;
        }
        return speed;
    }
}
