package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.Crook.add("minecraft:oak_leaves", "minecraft:stick", 0.1);}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.Crook")
public final class CrookCT {

    @ZenCodeType.Method
    public static void add(final String input, final String output, final double chance) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.add("drops", CraftTweakerBridge.drops(CraftTweakerBridge.drop(output, chance, 1, 1)));
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.ADD, root);
    }

    @ZenCodeType.Method
    public static void remove(final String input, final String output) {
        final JsonObject root = CraftTweakerBridge.input(input);
        final JsonArray remove = new JsonArray();
        remove.add(output);
        root.add("remove", remove);
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void removeInput(final String input) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("remove", true);
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void replace(final String input, final String output, final double chance) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.add("drops", CraftTweakerBridge.drops(CraftTweakerBridge.drop(output, chance, 1, 1)));
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REPLACE, root);
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void replaceJson(final String json) {
        CraftTweakerBridge.submit(TableId.CROOK, CustomizationOverlay.Action.REPLACE,
                CraftTweakerBridge.parse(json));
    }
}
