package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code CrookEvents.add('minecraft:oak_leaves', 'minecraft:stick', 0.1)}. */
public final class CrookEvents {

    public void add(final String input, final String output, final double chance) {
        final JsonObject root = KubeJSBridge.input(input);
        root.add("drops", KubeJSBridge.drops(KubeJSBridge.drop(output, chance, 1, 1)));
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.ADD, root);
    }

    public void remove(final String input, final String output) {
        final JsonObject root = KubeJSBridge.input(input);
        final JsonArray remove = new JsonArray();
        remove.add(output);
        root.add("remove", remove);
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE, root);
    }

    public void removeInput(final String input) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("remove", true);
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE, root);
    }

    public void replace(final String input, final String output, final double chance) {
        final JsonObject root = KubeJSBridge.input(input);
        root.add("drops", KubeJSBridge.drops(KubeJSBridge.drop(output, chance, 1, 1)));
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REPLACE, root);
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.ADD, KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE, KubeJSBridge.parse(json));
    }

    public void replaceJson(final String json) {
        KubeJSBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REPLACE, KubeJSBridge.parse(json));
    }
}
