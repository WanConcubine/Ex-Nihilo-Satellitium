package com.kaifengcrt.exnihilosatellitium.common.hammer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * <p>Gameplay always reads the server table; the client keeps a copy loaded
 * from the mirrored assets so JEI works when connected to a dedicated server.</p>
 */
public final class HammerRecipes {

    private static final String DIR = "hammer";
    private static final int MAX_COUNT = 64;

    private static volatile Map<Block, Rule> recipes = Map.of();
    private static volatile Map<Block, Rule> clientRecipes = Map.of();

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
        final Map<Block, Rule> built = new HashMap<>();
        int files = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation inputId = ResourceLocation.parse(root.get("input").getAsString());
                final Optional<Block> input = BuiltInRegistries.BLOCK.getOptional(inputId);
                if (input.isEmpty()) {
                    ExNihiloSatellitium.LOGGER.warn("[hammer] unknown input block {} in {}", inputId, id);
                    continue;
                }
                final ResourceLocation resultId = ResourceLocation.parse(root.get("result").getAsString());
                final Optional<Item> result = BuiltInRegistries.ITEM.getOptional(resultId);
                if (result.isEmpty() || result.get() == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[hammer] unknown result {} in {}", resultId, id);
                    continue;
                }
                final int min = root.has("min") ? root.get("min").getAsInt() : 1;
                final int max = root.has("max") ? root.get("max").getAsInt() : min;
                if (min < 1 || max < min || max > MAX_COUNT) {
                    ExNihiloSatellitium.LOGGER.warn("[hammer] invalid count range {}..{} in {}, skipped",
                            min, max, id);
                    continue;
                }
                if (built.putIfAbsent(input.get(), new Rule(result.get(), min, max)) != null) {
                    ExNihiloSatellitium.LOGGER.warn("[hammer] duplicate input {} in {}, later entry skipped",
                            inputId, id);
                    continue;
                }
                files++;
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[hammer] failed to read {}: {}", id, exception.toString());
            }
        }
        final Map<Block, Rule> snapshot = Map.copyOf(built);
        if (clientView) {
            clientRecipes = snapshot;
        } else {
            recipes = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[hammer] loaded {} recipes{}", files,
                clientView ? " (client)" : "");
    }
}
