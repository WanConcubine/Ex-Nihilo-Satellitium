package com.kaifengcrt.exnihilosatellitium.common.sieve;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sift table, loaded from {@code data/<namespace>/sieve/*.json}.
 *
 * <p>Each file describes one input and a list of drops; every drop is rolled
 * independently ({@code chance} per sift). The mesh tier does not change the
 * table, it only decides how many sifts a mesh survives.</p>
 */
public final class SieveRegistry {

    private static final String DIR = "sieve";
    /** Gameplay table (server); replaced wholesale on every reload. */
    private static volatile Map<Item, List<SieveDrop>> table = Map.of();
    /** Client-side copy for JEI; never used for server-side sifting. */
    private static volatile Map<Item, List<SieveDrop>> clientTable = Map.of();

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
        final Map<Item, List<SieveDrop>> built = new HashMap<>();
        int files = 0;
        int drops = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation inputId = ResourceLocation.parse(root.get("input").getAsString());
                final Item input = BuiltInRegistries.ITEM.getOptional(inputId).orElse(null);
                if (input == null || input == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[sieve] unknown input {} in {}", inputId, id);
                    continue;
                }
                final JsonArray outputs = root.getAsJsonArray("outputs");
                final List<SieveDrop> list = new ArrayList<>();
                for (final JsonElement element : outputs) {
                    final JsonObject drop = element.getAsJsonObject();
                    final ResourceLocation itemId = ResourceLocation.parse(drop.get("item").getAsString());
                    final Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                    final float chance = drop.get("chance").getAsFloat();
                    if (item == null || item == Items.AIR || !Float.isFinite(chance)
                            || chance <= 0.0F || chance > 1.0F) {
                        ExNihiloSatellitium.LOGGER.warn("[sieve] bad drop {} in {}", itemId, id);
                        continue;
                    }
                    list.add(new SieveDrop(item, chance));
                }
                if (built.containsKey(input)) {
                    ExNihiloSatellitium.LOGGER.warn("[sieve] duplicate input {} in {}, overwriting", inputId, id);
                }
                built.put(input, List.copyOf(list));
                files++;
                drops += list.size();
            } catch (Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[sieve] failed to read {}: {}", id, exception.toString());
            }
        }
        final Map<Item, List<SieveDrop>> snapshot = Map.copyOf(built);
        if (clientView) {
            clientTable = snapshot;
        } else {
            table = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[sieve] loaded {} inputs, {} drops{}",
                files, drops, clientView ? " (client)" : "");
    }
}
