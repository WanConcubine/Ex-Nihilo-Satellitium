package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code CrucibleHeatEvents.add('minecraft:torch', 0.1)}. */
public final class CrucibleHeatEvents {

    public void add(final String block, final double speed) {
        KubeJSBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.ADD,
                source(block, speed));
    }

    public void replace(final String block, final double speed) {
        KubeJSBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REPLACE,
                source(block, speed));
    }

    public void remove(final String block) {
        final JsonObject root = new JsonObject();
        root.addProperty("block", block);
        root.addProperty("remove", true);
        KubeJSBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REMOVE, root);
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.ADD,
                KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REMOVE,
                KubeJSBridge.parse(json));
    }

    private static JsonObject source(final String block, final double speed) {
        final JsonObject root = new JsonObject();
        root.addProperty("block", block);
        root.addProperty("speed", speed);
        return root;
    }
}
