package com.kaifengcrt.exnihilosatellitium.common.sieve;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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

    private SieveRegistry() {
    }

    public static List<SieveDrop> dropsFor(final ItemStack stack) {
        return table.getOrDefault(stack.getItem(), List.of());
    }

    /** Immutable snapshot for recipe browsers; key order is not meaningful. */
    public static Map<Item, List<SieveDrop>> all() {
        return table;
    }

    public static void load(final ResourceManager manager) {
        final Map<Item, List<SieveDrop>> built = new HashMap<>();
        int files = 0;
        int drops = 0;
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(DIR, path -> path.getPath().endsWith(".json")).entrySet()) {
            final ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                final ResourceLocation inputId = ResourceLocation.tryParse(root.get("input").getAsString());
                final Item input = inputId == null ? null : ForgeRegistries.ITEMS.getValue(inputId);
                if (input == null || input == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[sieve] unknown input {} in {}", inputId, id);
                    continue;
                }
                final JsonArray outputs = root.getAsJsonArray("outputs");
                final List<SieveDrop> list = new ArrayList<>();
                for (final JsonElement element : outputs) {
                    final JsonObject drop = element.getAsJsonObject();
                    // Optional gate for soft-dependency drops (e.g. Tinkers seeds):
                    // skip before touching the item registry, so an absent mod is silent.
                    final String requiredMod = drop.has("mod") ? drop.get("mod").getAsString() : null;
                    if (requiredMod != null && !Platform.isModLoaded(requiredMod)) {
                        continue;
                    }
                    final ResourceLocation itemId = ResourceLocation.tryParse(drop.get("item").getAsString());
                    final Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
                    final float chance = drop.get("chance").getAsFloat();
                    final int min = drop.has("min") ? drop.get("min").getAsInt() : 1;
                    final int max = drop.has("max") ? drop.get("max").getAsInt() : min;
                    if (item == null || item == Items.AIR || !Float.isFinite(chance)
                            || chance <= 0.0F || chance > 1.0F) {
                        ExNihiloSatellitium.LOGGER.warn("[sieve] bad drop {} in {}", itemId, id);
                        continue;
                    }
                    if (min < 1 || max < min) {
                        ExNihiloSatellitium.LOGGER.warn("[sieve] bad count {}..{} for {} in {}",
                                min, max, itemId, id);
                        continue;
                    }
                    list.add(new SieveDrop(item, chance, min, max));
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
        table = Map.copyOf(built);
        ExNihiloSatellitium.LOGGER.info("[sieve] loaded {} inputs, {} drops", files, drops);
    }
}
