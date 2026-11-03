package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code BarrelEvents.addItem('minecraft:water', 'minecraft:sugar', 'minecraft:dirt')}. */
public final class BarrelEvents {

    public void addItem(final String base, final String item, final String result) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD,
                recipe(base, "item", item, result));
    }

    public void addFluid(final String base, final String fluid, final String result) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD,
                recipe(base, "fluid", fluid, result));
    }

    public void replaceItem(final String base, final String item, final String result) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE,
                recipe(base, "item", item, result));
    }

    public void replaceFluid(final String base, final String fluid, final String result) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE,
                recipe(base, "fluid", fluid, result));
    }

    public void removeItem(final String base, final String item) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE,
                target(base, "item", item));
    }

    public void removeFluid(final String base, final String fluid) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE,
                target(base, "fluid", fluid));
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD, KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE, KubeJSBridge.parse(json));
    }

    public void replaceJson(final String json) {
        KubeJSBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE, KubeJSBridge.parse(json));
    }

    private static JsonObject recipe(final String base, final String kind, final String added,
                                     final String result) {
        final JsonObject root = target(base, kind, added);
        root.addProperty("result", result);
        return root;
    }

    private static JsonObject target(final String base, final String kind, final String added) {
        final JsonObject root = new JsonObject();
        root.addProperty("base", base);
        final JsonObject addedObject = new JsonObject();
        addedObject.addProperty(kind, added);
        root.add("added", addedObject);
        return root;
    }
}
