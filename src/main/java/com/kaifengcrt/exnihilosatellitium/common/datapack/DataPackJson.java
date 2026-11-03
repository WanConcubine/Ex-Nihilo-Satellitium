package com.kaifengcrt.exnihilosatellitium.common.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Small helpers shared by the data-pack driven gameplay tables.
 *
 * <p>Every table follows the same conventions:</p>
 * <ul>
 *   <li>files live in {@code data/<namespace>/<dir>/*.json} and are read from every namespace;</li>
 *   <li>an id field may be a concrete registry id or a {@code #tag}; tags are expanded at load
 *       time so the gameplay code keeps a plain registry-id lookup;</li>
 *   <li>{@code "mod": "x"} at the root skips the whole file when that mod is absent;</li>
 *   <li>{@code "patch": true} (or the presence of {@code "remove"}) makes the file a patch that
 *       is applied after all normal files, so pack authors can add/remove without copying a
 *       built-in file;</li>
 *   <li>{@code "remove": true} deletes the targeted entry, {@code "remove": ["id", ...]} removes
 *       individual list entries (sieve drops / crook drops).</li>
 * </ul>
 *
 * <p>This class is deliberately vanilla-only (plus the {@link Platform} facade) so it stays on
 * the core side of the platform boundary.</p>
 */
public final class DataPackJson {

    private DataPackJson() {
    }

    /** One parsed JSON file; {@code id} is the resource path, used for deterministic order. */
    public record Source(ResourceLocation id, JsonObject root) {
    }

    /**
     * Reads every {@code .json} under {@code data/<ns>/<dir>/}, sorted by resource id.
     *
     * <p>Sorting makes the "last write wins" and patch order deterministic instead of depending
     * on the resource manager's map iteration order.</p>
     */
    public static List<Source> readAll(final ResourceManager manager, final String dir,
                                       final String table) {
        final List<Source> sources = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, Resource> entry
                : manager.listResources(dir, path -> path.getPath().endsWith(".json")).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                final JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    ExNihiloSatellitium.LOGGER.warn("[{}] {} is not a JSON object", table, entry.getKey());
                    continue;
                }
                sources.add(new Source(entry.getKey(), element.getAsJsonObject()));
            } catch (final Exception exception) {
                ExNihiloSatellitium.LOGGER.error("[{}] failed to read {}: {}",
                        table, entry.getKey(), exception.toString());
            }
        }
        sources.sort(Comparator.comparing(source -> source.id().toString()));
        return List.copyOf(sources);
    }

    /**
     * {@code true} once the registry tags have been bound.
     *
     * <p>{@code TagManager} only stores its results during the reload; vanilla binds them
     * afterwards ({@code ReloadableServerResources#updateRegistryTags}), which fires
     * {@code TagsUpdatedEvent}. Before that, {@code #tag} inputs would expand to nothing.</p>
     */
    public static boolean tagsReady() {
        return BuiltInRegistries.ITEM.getTags().anyMatch(tag -> tag.getSecond().size() > 0);
    }

    /** {@code false} when the file declares {@code "mod"} and that mod is not installed. */
    public static boolean modLoaded(final JsonObject root) {
        if (!root.has("mod")) {
            return true;
        }
        final JsonElement mod = root.get("mod");
        if (!mod.isJsonPrimitive() || !mod.getAsJsonPrimitive().isString()) {
            return true;
        }
        return Platform.isModLoaded(mod.getAsString());
    }

    /** A patch is opt-in via {@code "patch": true}; any {@code remove} also implies a patch. */
    public static boolean isPatch(final JsonObject root) {
        if (root.has("remove")) {
            return true;
        }
        return root.has("patch")
                && root.get("patch").isJsonPrimitive()
                && root.get("patch").getAsJsonPrimitive().isBoolean()
                && root.get("patch").getAsBoolean();
    }

    /** {@code "remove": true} — delete the whole targeted entry/rule. */
    public static boolean removeAll(final JsonObject root) {
        if (!root.has("remove")) {
            return false;
        }
        final JsonElement remove = root.get("remove");
        return remove.isJsonPrimitive()
                && remove.getAsJsonPrimitive().isBoolean()
                && remove.getAsBoolean();
    }

    /** {@code "remove": ["minecraft:diamond", "#minecraft:saplings"]} — list-entry removals. */
    public static List<String> removeList(final JsonObject root) {
        if (!root.has("remove")) {
            return List.of();
        }
        final JsonElement remove = root.get("remove");
        if (!remove.isJsonArray()) {
            return List.of();
        }
        final List<String> ids = new ArrayList<>();
        for (final JsonElement element : remove.getAsJsonArray()) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                ids.add(element.getAsString());
            }
        }
        return List.copyOf(ids);
    }

    public static boolean isTag(final String raw) {
        return raw != null && raw.startsWith("#");
    }

    /** Parses an id field; returns {@code null} and warns when the text is malformed. */
    public static ResourceLocation id(final String raw, final String table,
                                      final ResourceLocation file) {
        if (raw == null) {
            ExNihiloSatellitium.LOGGER.warn("[{}] missing id field in {}", table, file);
            return null;
        }
        final String text = isTag(raw) ? raw.substring(1) : raw;
        final ResourceLocation id = ResourceLocation.tryParse(text);
        if (id == null) {
            ExNihiloSatellitium.LOGGER.warn("[{}] invalid id '{}' in {}", table, raw, file);
        }
        return id;
    }

    /**
     * Resolves a concrete registry id or a {@code #tag} to the current registry contents.
     *
     * <p>Tags are expanded here (after the tag reload listener has run), so callers never need
     * to know whether the author wrote an id or a tag.</p>
     */
    public static <T> Resolved<T> resolve(final Registry<T> registry, final String raw,
                                          final String table, final ResourceLocation file) {
        final boolean tag = isTag(raw);
        final ResourceLocation id = id(raw, table, file);
        if (id == null) {
            return new Resolved<>(List.of(), tag);
        }
        if (!tag) {
            final T value = registry.getOptional(id).orElse(null);
            if (value == null) {
                ExNihiloSatellitium.LOGGER.warn("[{}] unknown id {} in {}", table, id, file);
                return new Resolved<>(List.of(), false);
            }
            return new Resolved<>(List.of(value), false);
        }
        final TagKey<T> key = TagKey.create(registry.key(), id);
        final List<T> values = new ArrayList<>();
        for (final Holder<T> holder : registry.getTagOrEmpty(key)) {
            values.add(holder.value());
        }
        if (values.isEmpty()) {
            ExNihiloSatellitium.LOGGER.warn("[{}] empty or unknown tag #{} in {}", table, id, file);
        }
        return new Resolved<>(List.copyOf(values), true);
    }

    /** Expanded tag values plus whether the source text was a tag. */
    public record Resolved<T>(List<T> values, boolean tag) {
    }

    /** String field helper; {@code null} when absent or not a primitive. */
    public static String string(final JsonObject root, final String key) {
        if (!root.has(key)) {
            return null;
        }
        final JsonElement element = root.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }
        return element.getAsString();
    }
}
