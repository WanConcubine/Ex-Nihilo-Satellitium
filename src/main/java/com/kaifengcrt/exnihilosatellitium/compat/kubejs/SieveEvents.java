package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code SieveEvents.add('minecraft:sand', 'minecraft:diamond', 0.02)}. */
public final class SieveEvents {

    public void add(final String input, final String output, final double chance) {
        add(input, output, chance, 1, 1);
    }

    public void add(final String input, final String output, final double chance,
                    final int min, final int max) {
        final JsonObject root = KubeJSBridge.input(input);
        final JsonArray outputs = KubeJSBridge.drops(KubeJSBridge.drop(output, chance, min, max));
        root.add("outputs", outputs);
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.ADD, root);
    }

    public void remove(final String input, final String output) {
        final JsonObject root = KubeJSBridge.input(input);
        final JsonArray remove = new JsonArray();
        remove.add(output);
        root.add("remove", remove);
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE, root);
    }

    public void removeInput(final String input) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("remove", true);
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE, root);
    }

    public void replace(final String input, final String output, final double chance) {
        final JsonObject root = KubeJSBridge.input(input);
        root.add("outputs", KubeJSBridge.drops(KubeJSBridge.drop(output, chance, 1, 1)));
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REPLACE, root);
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.ADD, KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE, KubeJSBridge.parse(json));
    }

    public void replaceJson(final String json) {
        KubeJSBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REPLACE, KubeJSBridge.parse(json));
    }
}
