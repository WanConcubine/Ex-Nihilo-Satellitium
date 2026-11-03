package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.Hammer.add("minecraft:cobblestone", "minecraft:gravel");}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.Hammer")
public final class HammerCT {

    @ZenCodeType.Method
    public static void add(final String input, final String result) {
        add(input, result, 1, 1);
    }

    @ZenCodeType.Method
    public static void add(final String input, final String result, final int min, final int max) {
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.ADD,
                rule(input, result, min, max));
    }

    @ZenCodeType.Method
    public static void remove(final String input) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("remove", true);
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void replace(final String input, final String result, final int min, final int max) {
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REPLACE,
                rule(input, result, min, max));
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void replaceJson(final String json) {
        CraftTweakerBridge.submit(TableId.HAMMER, CustomizationOverlay.Action.REPLACE,
                CraftTweakerBridge.parse(json));
    }

    private static JsonObject rule(final String input, final String result,
                                   final int min, final int max) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("result", result);
        root.addProperty("min", min);
        root.addProperty("max", max);
        return root;
    }
}
