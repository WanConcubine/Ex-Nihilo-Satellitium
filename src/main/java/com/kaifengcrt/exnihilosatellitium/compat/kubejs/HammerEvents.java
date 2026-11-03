package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code HammerEvents.add('minecraft:cobblestone', 'minecraft:gravel')}. */
public final class HammerEvents {

    public void add(final String input, final String result) {
        add(input, result, 1, 1);
    }

    public void add(final String input, final String result, final int min, final int max) {
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.ADD,
                rule(input, result, min, max));
    }

    public void remove(final String input) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("remove", true);
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REMOVE, root);
    }

    public void replace(final String input, final String result, final int min, final int max) {
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REPLACE,
                rule(input, result, min, max));
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.ADD, KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REMOVE, KubeJSBridge.parse(json));
    }

    public void replaceJson(final String json) {
        KubeJSBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REPLACE, KubeJSBridge.parse(json));
    }

    private static JsonObject rule(final String input, final String result,
                                   final int min, final int max) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("result", result);
        root.addProperty("min", min);
        root.addProperty("max", max);
        return root;
    }
}
