package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code CompostEvents.add('minecraft:bread', 100)}. */
public final class CompostEvents {

    public void add(final String key, final int value) {
        final JsonObject values = new JsonObject();
        values.addProperty(key, value);
        final JsonObject root = new JsonObject();
        root.add("values", values);
        KubeJSBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.ADD, root);
    }

    public void remove(final String key) {
        final JsonObject root = new JsonObject();
        final JsonArray remove = new JsonArray();
        remove.add(key);
        root.add("remove", remove);
        KubeJSBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.REMOVE, root);
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.ADD, KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.REMOVE, KubeJSBridge.parse(json));
    }
}
