package com.kaifengcrt.exnihilosatellitium.common.crucible;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        final List<HeatSource> built = new ArrayList<>();
        int files = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation blockId = ResourceLocation.parse(root.get("block").getAsString());
                final Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(blockId);
                if (block.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] unknown heat block {} in {}", blockId, id);
                    continue;
                }
                final float speed = root.get("speed").getAsFloat();
                if (speed <= 0.0F || speed > MAX_SPEED) {
                    ExNihiloSatellitium.LOGGER.warn("[crucible] invalid heat speed {} in {}", speed, id);
                    continue;
                }
                final Map<String, String> properties = new HashMap<>();
                if (root.has("properties")) {
                    for (final Map.Entry<String, JsonElement> property
                            : root.getAsJsonObject("properties").entrySet()) {
                        properties.put(property.getKey(), property.getValue().getAsString());
                    }
                }
                built.add(new HeatSource(block.get(), Map.copyOf(properties), speed));
                files++;
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[crucible] failed to read heat source {}: {}",
                        id, exception.toString());
            }
        }
        final List<HeatSource> snapshot = List.copyOf(built);
        if (clientView) {
            clientSources = snapshot;
        } else {
            sources = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[crucible] loaded {} heat sources{}", files,
                clientView ? " (client)" : "");
    }
}
