package com.kaifengcrt.exnihilosatellitium.common.crook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extra leaf drops when a crook is used, loaded from
 * {@code data/<namespace>/crook/*.json}.
 *
 * <p>One file per leaves block: {@code input} is the leaves block and
 * {@code drops} is a list of item/chance entries. Vanilla leaf drops stay
 * untouched; the crook adds the listed entries on top.</p>
 */
public final class CrookDrops {

    public record Drop(Item item, float chance) {
    }

    private static final String DIR = "crook";
    private static volatile Map<Block, List<Drop>> table = Map.of();

    private CrookDrops() {
    }

    public static List<Drop> dropsFor(final Block block) {
        return table.getOrDefault(block, List.of());
    }

    public static void load(final ResourceManager manager) {
        final Map<Block, List<Drop>> built = new HashMap<>();
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
                    ExNihiloSatellitium.LOGGER.warn("[crook] unknown leaves block {} in {}", inputId, id);
                    continue;
                }
                final JsonArray drops = root.getAsJsonArray("drops");
                final List<Drop> list = new ArrayList<>();
                for (final JsonElement element : drops) {
                    final JsonObject drop = element.getAsJsonObject();
                    final ResourceLocation itemId = ResourceLocation.parse(drop.get("item").getAsString());
                    final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
                    final float chance = drop.get("chance").getAsFloat();
                    if (item.isEmpty() || item.get() == Items.AIR || !Float.isFinite(chance)
                            || chance <= 0.0F || chance > 1.0F) {
                        ExNihiloSatellitium.LOGGER.warn("[crook] bad drop {} in {}", itemId, id);
                        continue;
                    }
                    list.add(new Drop(item.get(), chance));
                }
                if (built.putIfAbsent(input.get(), List.copyOf(list)) != null) {
                    ExNihiloSatellitium.LOGGER.warn("[crook] duplicate input {} in {}, later entry skipped",
                            inputId, id);
                    continue;
                }
                files++;
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[crook] failed to read {}: {}", id, exception.toString());
            }
        }
        table = Map.copyOf(built);
        ExNihiloSatellitium.LOGGER.info("[crook] loaded {} leaves tables", files);
    }
}
