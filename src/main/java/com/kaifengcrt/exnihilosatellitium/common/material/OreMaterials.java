package com.kaifengcrt.exnihilosatellitium.common.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loads the built-in material table.
 *
 * <p>Read at mod construction time (before registries freeze), because every
 * material registers its own items and blocks. Changing the table therefore
 * needs a restart, not a {@code /reload}. The table lives in the jar; a config
 * override is not implemented yet (see {@code docs/tech-debt.md}).</p>
 */
public final class OreMaterials {

    private static final Pattern ID = Pattern.compile("[a-z0-9_]{1,64}");
    private static final Pattern COLOR = Pattern.compile("[0-9A-Fa-f]{6}");
    private static final String RESOURCE = "/exnihilosatellitium/materials.json";
    private static final Set<String> TIERS = Set.of("common", "uncommon", "rare", "epic");

    private static final List<OreMaterial> ALL = load();

    private OreMaterials() {
    }

    public static List<OreMaterial> all() {
        return ALL;
    }

    private static List<OreMaterial> load() {
        final List<OreMaterial> out = new ArrayList<>();
        final Map<String, String> seen = new LinkedHashMap<>();
        final InputStream stream = OreMaterials.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            ExNihiloSatellitium.LOGGER.error("[materials] missing {}; no materials registered", RESOURCE);
            return List.of();
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            final JsonArray materials = root.getAsJsonArray("materials");
            for (final JsonElement element : materials) {
                // One malformed entry must not hide the rest of the table.
                try {
                    final JsonObject entry = element.getAsJsonObject();
                    if (entry.has("enabled") && !entry.get("enabled").getAsBoolean()) {
                        continue;
                    }
                    final String id = entry.get("id").getAsString();
                    final String color = entry.get("color").getAsString();
                    final String tier = entry.get("tier").getAsString();
                    final int rarity = readRarity(entry, id);
                    final JsonObject name = entry.getAsJsonObject("name");
                    final String en = name.get("en_us").getAsString();
                    final String zh = name.has("zh_cn") ? name.get("zh_cn").getAsString() : en;
                    final String ingot = readIngot(entry, id);

                    if (!ID.matcher(id).matches()) {
                        ExNihiloSatellitium.LOGGER.error("[materials] bad id '{}', skipped", id);
                        continue;
                    }
                    if (!COLOR.matcher(color).matches()) {
                        ExNihiloSatellitium.LOGGER.error("[materials] bad color '{}' for {}, skipped", color, id);
                        continue;
                    }
                    if (!TIERS.contains(tier)) {
                        ExNihiloSatellitium.LOGGER.error("[materials] bad tier '{}' for {}, skipped", tier, id);
                        continue;
                    }
                    if (!rarityInTier(tier, rarity)) {
                        ExNihiloSatellitium.LOGGER.error(
                                "[materials] rarity {} is outside tier '{}' for {}, skipped", rarity, tier, id);
                        continue;
                    }
                    if (seen.putIfAbsent(id, tier) != null) {
                        ExNihiloSatellitium.LOGGER.error("[materials] duplicate id '{}', later entry skipped", id);
                        continue;
                    }
                    out.add(new OreMaterial(id, Integer.parseInt(color, 16), en, zh, tier, rarity, ingot));
                } catch (final Exception exception) {
                    ExNihiloSatellitium.LOGGER.error("[materials] bad entry skipped: {}", exception.toString());
                }
            }
        } catch (final Exception exception) {
            ExNihiloSatellitium.LOGGER.error("[materials] could not read {}: {}", RESOURCE, exception.toString());
        }
        ExNihiloSatellitium.LOGGER.info("[materials] loaded {} entries", out.size());
        return List.copyOf(out);
    }

    /**
     * A single {@code 1/N} value. The per-kind object form from the old design
     * notes is not implemented; use the gravel value and keep the three outputs
     * aligned, or split the field in a later schema revision.
     */
    private static int readRarity(final JsonObject entry, final String id) {
        final JsonElement rarity = entry.get("rarity");
        if (rarity.isJsonPrimitive()) {
            return rarity.getAsInt();
        }
        ExNihiloSatellitium.LOGGER.warn(
                "[materials] {}: per-kind rarity object is not implemented; using rarity.gravel", id);
        return rarity.getAsJsonObject().get("gravel").getAsInt();
    }

    private static boolean rarityInTier(final String tier, final int rarity) {
        return switch (tier) {
            case "common" -> rarity > 0 && rarity <= 24;
            case "uncommon" -> rarity >= 25 && rarity <= 48;
            case "rare" -> rarity >= 49 && rarity <= 96;
            case "epic" -> rarity >= 97;
            default -> false;
        };
    }

    /**
     * Optional {@code ingot}: a concrete item id to reuse (for example
     * {@code minecraft:copper_ingot}). {@code null} means "register our own
     * {@code <id>_ingot}", which keeps the chain playable when no other mod
     * supplies that metal.
     *
     * <p>Tags are not accepted yet: tags are not loaded when this table is read,
     * and the smelting recipes are generated as static JSON from the table.</p>
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
}
