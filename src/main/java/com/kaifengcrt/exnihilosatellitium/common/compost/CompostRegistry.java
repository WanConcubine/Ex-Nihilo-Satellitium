package com.kaifengcrt.exnihilosatellitium.common.compost;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data-driven compost values.
 *
 * <p>Loaded from {@code data/<namespace>/compost/*.json} on server resource
 * reload. Values are additive: a sapling is worth 125, a leaf block 125, and
 * 1000 points fill the barrel. The scale is Ex Nihilo Omnia's 1000 mB table,
 * so 0.125 becomes 125 and the 1/6 melon/pumpkin value becomes 150.</p>
 *
 * <p>The server reads it for barrel interactions. The client also reads the
 * mirrored {@code assets/.../compost/} copy so JEI can show the table even
 * when connected to a dedicated server; interaction prediction still uses the
 * {@code #exnihilosatellitium:compostable} item tag.</p>
 */
public final class CompostRegistry {

    public record Entry(Item item, TagKey<Item> tag, int value) {

        public boolean matches(final ItemStack stack) {
            return this.item != null ? stack.is(this.item) : stack.is(this.tag);
        }
    }

    /** Gameplay table (server); replaced wholesale on every reload. */
    private static volatile List<Entry> entries = List.of();
    /** Client-side copy for JEI; never used for server-side composting. */
    private static volatile List<Entry> clientEntries = List.of();

    private CompostRegistry() {
    }

    /** Immutable snapshot for recipe browsers (JEI). */
    public static List<Entry> all() {
        return entries.isEmpty() ? clientEntries : entries;
    }

    public static void reload(final Map<ResourceLocation, JsonElement> jsons, final boolean clientView) {
        final List<Entry> next = new ArrayList<>();
        final List<Map.Entry<ResourceLocation, JsonElement>> sorted = new ArrayList<>(jsons.entrySet());
        sorted.sort(Comparator.comparing(file -> file.getKey().toString()));

        // Normal files first, so a patch always sees the full built-in table.
        for (final Map.Entry<ResourceLocation, JsonElement> file : sorted) {
            try {
                final JsonObject root = file.getValue().getAsJsonObject();
                if (!DataPackJson.modLoaded(root) || DataPackJson.isPatch(root)) {
                    continue;
                }
                addValues(root, file.getKey(), next);
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.warn("[compost] failed to parse {}", file.getKey(), exception);
            }
        }

        for (final Map.Entry<ResourceLocation, JsonElement> file : sorted) {
            try {
                final JsonObject root = file.getValue().getAsJsonObject();
                if (!DataPackJson.modLoaded(root) || !DataPackJson.isPatch(root)) {
                    continue;
                }
                final Set<Item> removeItems = new LinkedHashSet<>();
                final Set<TagKey<Item>> removeTags = new LinkedHashSet<>();
                for (final String raw : DataPackJson.removeList(root)) {
                    if (DataPackJson.isTag(raw)) {
                        final ResourceLocation id = DataPackJson.id(raw, "compost", file.getKey());
                        if (id != null) {
                            removeTags.add(TagKey.create(Registries.ITEM, id));
                        }
                    } else {
                        final ResourceLocation id = DataPackJson.id(raw, "compost", file.getKey());
                        final Item item = id == null
                                ? null
                                : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                        if (item != null && item != Items.AIR) {
                            removeItems.add(item);
                        }
                    }
                }
                if (!removeItems.isEmpty() || !removeTags.isEmpty()) {
                    next.removeIf(entry -> (entry.item() != null && removeItems.contains(entry.item()))
                            || (entry.tag() != null && removeTags.contains(entry.tag())));
                }
                addValues(root, file.getKey(), next);
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.warn("[compost] failed to patch {}", file.getKey(), exception);
            }
        }

        final List<Entry> snapshot = List.copyOf(next);
        if (clientView) {
            clientEntries = snapshot;
        } else {
            entries = snapshot;
        }
        ExNihiloSatellitium.LOGGER.info("[compost] loaded {} entries{}",
                snapshot.size(), clientView ? " (client)" : "");
    }

    private static void addValues(final JsonObject root, final ResourceLocation file,
                                  final List<Entry> next) {
        if (!root.has("values") || !root.get("values").isJsonObject()) {
            return;
        }
        for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("values").entrySet()) {
            final String key = entry.getKey();
            final int value = entry.getValue().getAsInt();
            if (value <= 0) {
                continue;
            }

            if (key.startsWith("#")) {
                final ResourceLocation id = ResourceLocation.tryParse(key.substring(1));
                if (id == null) {
                    ExNihiloSatellitium.LOGGER.warn("[compost] invalid tag id '{}' in {}", key, file);
                    continue;
                }
                next.add(new Entry(null, TagKey.create(Registries.ITEM, id), value));
            } else {
                final ResourceLocation id = ResourceLocation.tryParse(key);
                if (id == null) {
                    ExNihiloSatellitium.LOGGER.warn("[compost] invalid item id '{}' in {}", key, file);
                    continue;
                }
                final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                if (item == null || item == Items.AIR) {
                    ExNihiloSatellitium.LOGGER.warn("[compost] unknown item {}", id);
                    continue;
                }
                next.add(new Entry(item, null, value));
            }
        }
    }

    /** @return the best matching compost value, or 0 if the stack is not compostable. */
    public static int valueOf(final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int best = 0;
        for (final Entry entry : entries) {
            if (entry.matches(stack)) {
                best = Math.max(best, entry.value());
            }
        }
        return best;
    }
}
