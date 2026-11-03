package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.Compost.add("minecraft:bread", 100);}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.Compost")
public final class CompostCT {

    @ZenCodeType.Method
    public static void add(final String key, final int value) {
        final JsonObject values = new JsonObject();
        values.addProperty(key, value);
        final JsonObject root = new JsonObject();
        root.add("values", values);
        CraftTweakerBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.ADD, root);
    }

    @ZenCodeType.Method
    public static void remove(final String key) {
        final JsonObject root = new JsonObject();
        final JsonArray remove = new JsonArray();
        remove.add(key);
        root.add("remove", remove);
        CraftTweakerBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.COMPOST, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }
}
