package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.CrucibleHeat.add("minecraft:torch", 0.1);}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.CrucibleHeat")
public final class CrucibleHeatCT {

    @ZenCodeType.Method
    public static void add(final String block, final double speed) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.ADD,
                source(block, speed));
    }

    @ZenCodeType.Method
    public static void replace(final String block, final double speed) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REPLACE,
                source(block, speed));
    }

    @ZenCodeType.Method
    public static void remove(final String block) {
        final JsonObject root = new JsonObject();
        root.addProperty("block", block);
        root.addProperty("remove", true);
        CraftTweakerBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_HEAT, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    private static JsonObject source(final String block, final double speed) {
        final JsonObject root = new JsonObject();
        root.addProperty("block", block);
        root.addProperty("speed", speed);
        return root;
    }
}
