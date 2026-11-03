package com.kaifengcrt.exnihilosatellitium.common.crucible;

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
 * again on {@code TagsUpdatedEvent}.</p>
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
        rebuild(clientView);
    }

    /** Rebuilds from the last read sources; called again after tags are bound. */
    public static void rebuild(final boolean clientView) {
        final List<DataPackJson.Source> sourceList = clientView ? clientFiles : files;
        final boolean tagsReady = DataPackJson.tagsReady();
        final List<HeatSource> built = new ArrayList<>();
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
            final HeatSource heat = parseSource(source.root(), source.id());
            if (heat != null) {
                built.add(heat);
            }
        }

        if (tagsReady) {
            for (final DataPackJson.Source source : tagFiles) {
                final List<Block> inputs = blocks(DataPackJson.string(source.root(), "block"), source.id());
                final Map<String, String> properties = properties(source.root());
                final float speed = speed(source.root(), source.id());
                if (speed <= 0.0F) {
                    continue;
                }
                for (final Block input : inputs) {
                    if (!hasSource(built, input, properties)) {
                        built.add(new HeatSource(input, properties, speed));
                    }
                }
            }

            for (final DataPackJson.Source source : patchFiles) {
                final List<Block> inputs = blocks(DataPackJson.string(source.root(), "block"), source.id());
                final Map<String, String> properties = properties(source.root());
                built.removeIf(heat -> inputs.contains(heat.block())
                        && (properties.isEmpty() || properties.equals(heat.properties())));
                if (DataPackJson.removeAll(source.root())) {
                    continue;
                }
                final float speed = speed(source.root(), source.id());
                if (speed <= 0.0F) {
                    continue;
                }
                for (final Block input : inputs) {
                    built.add(new HeatSource(input, properties, speed));
                }
            }
        }

        final List<HeatSource> snapshot = List.copyOf(built);
        if (clientView) {
            clientSources = snapshot;
        } else {
            sources = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[crucible] loaded {} heat sources ({} tag files, {} patches{}){}",
                built.size(), tagFiles.size(), patchFiles.size(),
                tagsReady ? "" : ", tags not ready", clientView ? " (client)" : "");
    }

    private static boolean hasSource(final List<HeatSource> built, final Block block,
                                     final Map<String, String> properties) {
        for (final HeatSource heat : built) {
            if (heat.block() == block && heat.properties().equals(properties)) {
                return true;
            }
        }
        return false;
    }

    private static HeatSource parseSource(final JsonObject root, final ResourceLocation file) {
        final List<Block> inputs = blocks(DataPackJson.string(root, "block"), file);
        final float speed = speed(root, file);
        if (inputs.isEmpty() || speed <= 0.0F) {
            return null;
        }
        return new HeatSource(inputs.get(0), properties(root), speed);
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
