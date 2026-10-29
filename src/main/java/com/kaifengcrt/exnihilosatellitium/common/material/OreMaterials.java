package com.kaifengcrt.exnihilosatellitium.common.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;

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
                out.add(new OreMaterial(id, Integer.parseInt(color, 16), en, zh, tier, rarity));
            }
        } catch (Exception exception) {
            ExNihiloSatellitium.LOGGER.error("[materials] could not read {}: {}", RESOURCE, exception.toString());
        }
        ExNihiloSatellitium.LOGGER.info("[materials] loaded {} entries", out.size());
        return List.copyOf(out);
    }
}
