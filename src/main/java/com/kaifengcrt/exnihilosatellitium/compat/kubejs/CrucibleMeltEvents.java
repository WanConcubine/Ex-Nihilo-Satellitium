package com.kaifengcrt.exnihilosatellitium.compat.kubejs;

import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;

/** KubeJS binding: {@code CrucibleMeltEvents.add('minecraft:cobblestone', 'minecraft:lava', 250)}. */
public final class CrucibleMeltEvents {

    public void add(final String input, final String fluid, final int amount) {
        KubeJSBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.ADD,
                melt(input, fluid, amount));
    }

    public void replace(final String input, final String fluid, final int amount) {
        KubeJSBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REPLACE,
                melt(input, fluid, amount));
    }

    public void remove(final String input) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("remove", true);
        KubeJSBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REMOVE, root);
    }

    public void addJson(final String json) {
        KubeJSBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.ADD,
                KubeJSBridge.parse(json));
    }

    public void removeJson(final String json) {
        KubeJSBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REMOVE,
                KubeJSBridge.parse(json));
    }

    private static JsonObject melt(final String input, final String fluid, final int amount) {
        final JsonObject root = KubeJSBridge.input(input);
        root.addProperty("fluid", fluid);
        root.addProperty("amount", amount);
        root.addProperty("solid", amount);
        return root;
    }
}
