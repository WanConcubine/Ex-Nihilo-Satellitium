package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/**
 * Small shared bridge between the KubeJS bindings and the customization overlay.
 *
 * <p>KubeJS contributes at {@link #PRIORITY} (100), so it is applied after every
 * datapack file/patch (priority 0) but before a future CraftTweaker tier.
 * Scripts call the bindings at load time; the actual table rebuild happens once
 * on KubeJS's {@code ServerEvents.loaded}, so adding 100 entries is one rebuild,
 * not 100.</p>
 */
final class KubeJSBridge {

    static final String SOURCE = "kubejs";
    static final int PRIORITY = 100;

    private KubeJSBridge() {
    }

    static void submit(final TableId table, final CustomizationOverlay.Action action,
                       final JsonObject payload) {
        if (CustomizationOverlay.submit(table, action, payload, SOURCE, PRIORITY) == null) {
            throw new IllegalArgumentException(
                    "exnihilosatellitium rejected the KubeJS " + table.id() + " " + action
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
