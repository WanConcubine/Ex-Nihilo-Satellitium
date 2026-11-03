package com.kaifengcrt.exnihilosatellitium.common.compost;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.datapack.DataPackJson;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableBuilder;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
 *
 * <p>Entries are keyed by the literal JSON key (item id or {@code #tag}), so a
 * patch or overlay operation can delete exactly one value. Lookups keep the
 * "highest matching value wins" rule, which is why the loader merges duplicate
 * keys with {@code max} instead of last-write-wins.</p>
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
    /** Last parsed files, kept so an overlay change can rebuild without a resource reload. */
    private static volatile Map<ResourceLocation, JsonElement> sources = Map.of();
    private static volatile Map<ResourceLocation, JsonElement> clientSources = Map.of();

    private CompostRegistry() {
    }

    /** Immutable snapshot for recipe browsers (JEI). */
    public static List<Entry> all() {
        return entries.isEmpty() ? clientEntries : entries;
    }

    public static void reload(final Map<ResourceLocation, JsonElement> jsons, final boolean clientView) {
        if (clientView) {
            clientSources = Map.copyOf(jsons);
        } else {
            sources = Map.copyOf(jsons);
        }
        rebuild(clientView);
    }

    /** Rebuilds from the last read files; also called after an overlay change. */
    public static void rebuild(final boolean clientView) {
        final Map<ResourceLocation, JsonElement> jsons = clientView ? clientSources : sources;
        if (jsons.isEmpty()) {
            return;
        }
        final TableBuilder<String, Entry> builder = new TableBuilder<>(TableId.COMPOST.id());
        final List<Map.Entry<ResourceLocation, JsonElement>> sorted = new ArrayList<>(jsons.entrySet());
        sorted.sort(Comparator.comparing(file -> file.getKey().toString()));

        try {
            // Normal files first, so a patch always sees the full built-in table.
            for (final Map.Entry<ResourceLocation, JsonElement> file : sorted) {
                try {
                    final JsonObject root = file.getValue().getAsJsonObject();
                    if (!DataPackJson.modLoaded(root) || DataPackJson.isPatch(root)) {
                        continue;
                    }
                    addValues(builder, root, file.getKey(), false);
                } catch (final Exception exception) {
                    ExNihiloSatellitium.LOGGER.warn("[compost] failed to parse {}",
                            file.getKey(), exception);
                }
            }

            for (final Map.Entry<ResourceLocation, JsonElement> file : sorted) {
                try {
                    final JsonObject root = file.getValue().getAsJsonObject();
                    if (!DataPackJson.modLoaded(root) || !DataPackJson.isPatch(root)) {
                        continue;
                    }
                    if (DataPackJson.removeAll(root)) {
                        // "remove": true without a list cannot target anything meaningful.
                        ExNihiloSatellitium.LOGGER.warn(
                                "[compost] {} needs a remove list, ignoring remove:true", file.getKey());
                    } else {
                        removeKeys(builder, DataPackJson.removeList(root), null);
                    }
                    addValues(builder, root, file.getKey(), false);
                } catch (final Exception exception) {
                    ExNihiloSatellitium.LOGGER.warn("[compost] failed to patch {}",
                            file.getKey(), exception);
                }
            }

            applyOverlay(builder);
        } catch (final RuntimeException exception) {
            ExNihiloSatellitium.LOGGER.error("[compost] rebuild failed, keeping previous snapshot",
                    exception);
            return;
        }

        final List<Entry> snapshot = List.copyOf(builder.build().values());
        if (clientView) {
            clientEntries = snapshot;
        } else {
            entries = snapshot;
            builder.logConflicts();
        }
        ExNihiloSatellitium.LOGGER.info("[compost] loaded {} entries ({} overlay){}",
                snapshot.size(), CustomizationOverlay.forTable(TableId.COMPOST).size(),
                clientView ? " (client)" : "");
    }

    private static void applyOverlay(final TableBuilder<String, Entry> builder) {
        for (final CustomizationOverlay.Customization customization
                : CustomizationOverlay.forTable(TableId.COMPOST)) {
            try {
                final JsonObject root = customization.payload();
                final boolean replace = customization.action() == CustomizationOverlay.Action.REPLACE;
                if (replace) {
                    removeKeys(builder, DataPackJson.removeList(root), null);
                }
                if (customization.action() == CustomizationOverlay.Action.REMOVE) {
                    List<String> keys = DataPackJson.removeList(root);
                    if (keys.isEmpty()) {
                        keys = new ArrayList<>(valueKeys(root));
                    }
                    removeKeys(builder, keys,
                            "customize:" + customization.source() + "#" + customization.sequence());
                    continue;
                }
                addValues(builder, root, overlayLabel(customization), replace);
            } catch (final RuntimeException exception) {
                ExNihiloSatellitium.LOGGER.warn("[compost] overlay {} failed: {}",
                        customization.describe(), exception.toString());
            }
        }
    }

    private static List<String> valueKeys(final JsonObject root) {
        final List<String> keys = new ArrayList<>();
        if (root.has("values") && root.get("values").isJsonObject()) {
            keys.addAll(root.getAsJsonObject("values").keySet());
        }
        return keys;
    }

    private static void removeKeys(final TableBuilder<String, Entry> builder, final List<String> keys,
                                   final String source) {
        for (final String raw : keys) {
            builder.remove(normalizeKey(raw), source);
        }
    }

    private static void addValues(final TableBuilder<String, Entry> builder, final JsonObject root,
                                  final ResourceLocation file, final boolean replace) {
        if (!root.has("values") || !root.get("values").isJsonObject()) {
            return;
        }
        for (final Map.Entry<String, JsonElement> entry : root.getAsJsonObject("values").entrySet()) {
            final String rawKey = entry.getKey();
            final int value = entry.getValue().getAsInt();
            if (value <= 0) {
                continue;
            }
            final String key = normalizeKey(rawKey);
            final Entry parsed = parseEntry(rawKey, key, value, file);
            if (parsed == null) {
                continue;
            }
            if (replace) {
                builder.put(key, parsed, file.toString());
            } else {
                builder.merge(key, parsed, CompostRegistry::higher, file.toString());
            }
        }
    }

    private static Entry higher(final Entry left, final Entry right) {
        return left.value() >= right.value() ? left : right;
    }

    /** Item ids keep their form; tag keys are stored with the leading {@code #}. */
    private static String normalizeKey(final String raw) {
        final String key = raw.startsWith("#") ? raw.substring(1) : raw;
        return raw.startsWith("#") ? "#" + key : key;
    }

    private static Entry parseEntry(final String rawKey, final String key, final int value,
                                    final ResourceLocation file) {
        if (rawKey.startsWith("#")) {
            final ResourceLocation id = ResourceLocation.tryParse(rawKey.substring(1));
            if (id == null) {
                ExNihiloSatellitium.LOGGER.warn("[compost] invalid tag id '{}' in {}", rawKey, file);
                return null;
            }
            return new Entry(null, TagKey.create(Registries.ITEM, id), value);
        }
        final ResourceLocation id = ResourceLocation.tryParse(rawKey);
        if (id == null) {
            ExNihiloSatellitium.LOGGER.warn("[compost] invalid item id '{}' in {}", rawKey, file);
            return null;
        }
        final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null || item == Items.AIR) {
            ExNihiloSatellitium.LOGGER.warn("[compost] unknown item {}", id);
            return null;
        }
        return new Entry(item, null, value);
    }

    private static ResourceLocation overlayLabel(final CustomizationOverlay.Customization customization) {
        final ResourceLocation label = ResourceLocation.tryParse(
                "customize:compost/" + customization.sequence());
        return label == null ? ResourceLocation.tryParse("customize:compost/0") : label;
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
