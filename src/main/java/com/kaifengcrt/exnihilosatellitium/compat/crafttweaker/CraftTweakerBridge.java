package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/**
 * Shared bridge for the CraftTweaker Zen classes.
 *
 * <p>CT contributes at priority 200, so it is applied after datapack files,
 * patches and the KubeJS tier (100). Operations are queued at script-load time;
 * the tables are rebuilt by the normal tag reload / server start.</p>
 */
final class CraftTweakerBridge {

    static final String SOURCE = "crafttweaker";
    static final int PRIORITY = 200;

    private CraftTweakerBridge() {
    }

    static void submit(final TableId table, final CustomizationOverlay.Action action,
                       final JsonObject payload) {
        if (CustomizationOverlay.submit(table, action, payload, SOURCE, PRIORITY) == null) {
            throw new IllegalArgumentException(
                    "exnihilosatellitium rejected the CraftTweaker " + table.id() + " " + action
                            + " operation (empty payload or operation cap)");
        }
    }

    static JsonObject parse(final String raw) {
        final JsonElement element = JsonParser.parseString(raw);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("payload must be a JSON object: " + raw);
        }
        return element.getAsJsonObject();
    }

    static JsonObject drop(final String item, final double chance, final int min, final int max) {
        final JsonObject drop = new JsonObject();
        drop.addProperty("item", item);
        drop.addProperty("chance", chance);
        if (min != 1 || max != 1) {
            drop.addProperty("min", min);
            drop.addProperty("max", max);
        }
        return drop;
    }

    static JsonObject input(final String input) {
        final JsonObject root = new JsonObject();
        root.addProperty("input", input);
        return root;
    }

    static JsonArray drops(final JsonObject drop) {
        final JsonArray array = new JsonArray();
        array.add(drop);
        return array;
    }
}
