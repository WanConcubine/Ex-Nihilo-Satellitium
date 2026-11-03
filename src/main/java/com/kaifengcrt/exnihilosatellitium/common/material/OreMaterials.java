package com.kaifengcrt.exnihilosatellitium.common.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Loads the built-in material table.
 *
 * <p>Read at mod construction time (before registries freeze), because every
 * material registers its own items and blocks. Changing the table therefore
 * needs a restart, not a {@code /reload}.</p>
 */
public final class OreMaterials {

    private static final Pattern ID = Pattern.compile("[a-z0-9_]{1,64}");
    private static final Pattern COLOR = Pattern.compile("[0-9A-Fa-f]{6}");
    private static final String RESOURCE = "/exnihilosatellitium/materials.json";
    /**
     * Set by the {@code runData} JVM. Datagen must see every material so it can emit
     * the assets for mod-gated {@code gems} entries; a normal game only registers a
     * gem material when one of its candidate mods is actually loaded.
     */
    private static final boolean DATAGEN = Boolean.getBoolean("exnihilosatellitium.datagen");

    private static final List<OreMaterial> ALL = load();

    private OreMaterials() {
    }

    public static List<OreMaterial> all() {
        return ALL;
    }

    private static List<OreMaterial> load() {
        final List<OreMaterial> out = new ArrayList<>();
        final Map<String, String> seen = new LinkedHashMap<>();
        try (InputStreamReader reader = new InputStreamReader(
                OreMaterials.class.getResourceAsStream(RESOURCE), StandardCharsets.UTF_8)) {
            final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            final JsonArray materials = root.getAsJsonArray("materials");
            for (final JsonElement element : materials) {
                final JsonObject entry = element.getAsJsonObject();
                if (entry.has("enabled") && !entry.get("enabled").getAsBoolean()) {
                    continue;
                }
                final String id = entry.get("id").getAsString();
                final String color = entry.get("color").getAsString();
                final int rarity = entry.get("rarity").isJsonPrimitive()
                        ? entry.get("rarity").getAsInt()
                        : entry.getAsJsonObject("rarity").get("gravel").getAsInt();
                final String tier = entry.get("tier").getAsString();
                final JsonObject name = entry.getAsJsonObject("name");
                final String en = name.get("en_us").getAsString();
                final String zh = name.has("zh_cn") ? name.get("zh_cn").getAsString() : en;

                // A material is either a metal (ingot/ingots) or a mineral (gems).
                // Mixing the two would make the fallback item and its tag ambiguous.
                final boolean gem = entry.has("gems");
                if (gem && (entry.has("ingot") || entry.has("ingots"))) {
                    ExNihiloSatellitium.LOGGER.error(
                            "[materials] {}: 'gems' cannot be combined with 'ingot'/'ingots', skipped", id);
                    continue;
                }
                // An unconditional "ingot" wins over the candidate list, so the
                // candidates are dropped in that case.
                final String explicitProduct = gem ? null : readIngot(entry, id);
                final List<OreMaterial.ProductCandidate> candidates = explicitProduct == null
                        ? readCandidates(entry, gem ? "gems" : "ingots", id) : List.of();
                if (gem && !DATAGEN && !anyCandidateLoaded(candidates)) {
                    ExNihiloSatellitium.LOGGER.info(
                            "[materials] {} not registered: none of its supporting mods is loaded", id);
                    continue;
                }
                final String product = resolveProduct(explicitProduct, candidates, id);

                if (!ID.matcher(id).matches()) {
                    ExNihiloSatellitium.LOGGER.error("[materials] bad id '{}', skipped", id);
                    continue;
                }
                if (!COLOR.matcher(color).matches()) {
                    ExNihiloSatellitium.LOGGER.error("[materials] bad color '{}' for {}, skipped", color, id);
                    continue;
                }
                if (rarity <= 0) {
                    ExNihiloSatellitium.LOGGER.error("[materials] rarity must be > 0 for {}, skipped", id);
                    continue;
                }
                final String previous = seen.putIfAbsent(id, tier);
                if (previous != null) {
                    ExNihiloSatellitium.LOGGER.error("[materials] duplicate id '{}', later entry skipped", id);
                    continue;
                }
                out.add(new OreMaterial(id, Integer.parseInt(color, 16), en, zh, tier, rarity, product,
                        candidates, gem));
            }
        } catch (Exception exception) {
            ExNihiloSatellitium.LOGGER.error("[materials] could not read {}: {}", RESOURCE, exception.toString());
        }
        ExNihiloSatellitium.LOGGER.info("[materials] loaded {} entries", out.size());
        return List.copyOf(out);
    }

    /**
     * Optional {@code ingot}: an existing item id to smelt into (for example
     * {@code minecraft:iron_ingot}). {@code null} means "register our own
     * {@code <id>_ingot}". Tags are not supported yet: tags are not loaded when
     * this table is read, so a {@code #...} value falls back to our own ingot.
     */
    private static String readIngot(final JsonObject entry, final String id) {
        if (!entry.has("ingot") || entry.get("ingot").isJsonNull()) {
            return null;
        }
        final String value = entry.get("ingot").getAsString();
        if (value.startsWith("#")) {
            ExNihiloSatellitium.LOGGER.warn("[materials] {}: tag ingot '{}' is not supported yet, "
                    + "registering own {}_ingot", id, value, id);
            return null;
        }
        if (ResourceLocation.tryParse(value) == null) {
            ExNihiloSatellitium.LOGGER.error("[materials] {}: bad ingot '{}', registering own {}_ingot",
                    id, value, id);
            return null;
        }
        return value;
    }

    /**
     * Parses the ordered candidate list from {@code field} ({@code "ingots"} or
     * {@code "gems"}), each entry {@code {"item": "...", "mod": "..."}}. Entries
     * without {@code item} or with an unparseable id are skipped with a log line;
     * a missing {@code mod} makes the candidate unconditional.
     */
    private static List<OreMaterial.ProductCandidate> readCandidates(final JsonObject entry,
                                                                     final String field,
                                                                     final String id) {
        final JsonElement candidates = entry.get(field);
        if (candidates == null || !candidates.isJsonArray()) {
            return List.of();
        }
        final List<OreMaterial.ProductCandidate> out = new ArrayList<>();
        for (final JsonElement element : candidates.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                ExNihiloSatellitium.LOGGER.warn("[materials] {}: skipping a non-object {} entry", id, field);
                continue;
            }
            final JsonObject candidate = element.getAsJsonObject();
            if (!candidate.has("item")) {
                ExNihiloSatellitium.LOGGER.warn("[materials] {}: skipping a {} entry without 'item'", id, field);
                continue;
            }
            final String item = candidate.get("item").getAsString();
            if (ResourceLocation.tryParse(item) == null) {
                ExNihiloSatellitium.LOGGER.error("[materials] {}: bad {} item '{}', skipped", id, field, item);
                continue;
            }
            final String mod = candidate.has("mod") ? candidate.get("mod").getAsString() : null;
            out.add(new OreMaterial.ProductCandidate(item, mod));
        }
        return List.copyOf(out);
    }

    /**
     * Resolves the item the ore blocks smelt into: the first candidate whose mod
     * is loaded, else our own fallback ({@code <id>_ingot}, or {@code <id>} for
     * gems; returned as {@code null}).
     *
     * <p>The hand-written conditional smelting recipes carry the same order, so the
     * JSON data and this runtime resolution agree without the recipe existing twice
     * at load time.</p>
     */
    private static String resolveProduct(final String explicit,
                                         final List<OreMaterial.ProductCandidate> candidates,
                                         final String id) {
        if (explicit != null) {
            return explicit;
        }
        for (final OreMaterial.ProductCandidate candidate : candidates) {
            if (candidate.mod() != null && !isModLoaded(candidate.mod())) {
                continue;
            }
            ExNihiloSatellitium.LOGGER.info("[materials] {} smelts into {}{}", id, candidate.item(),
                    candidate.mod() == null ? "" : " from " + candidate.mod());
            return candidate.item();
        }
        return null;
    }

    /** True when at least one candidate is unconditional or its mod is loaded. */
    private static boolean anyCandidateLoaded(final List<OreMaterial.ProductCandidate> candidates) {
        for (final OreMaterial.ProductCandidate candidate : candidates) {
            if (candidate.mod() == null || isModLoaded(candidate.mod())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModLoaded(final String mod) {
        return ModList.get() != null && ModList.get().isLoaded(mod);
    }
}
