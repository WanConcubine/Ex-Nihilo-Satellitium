package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.Barrel.addItem("minecraft:water", "minecraft:sugar", "minecraft:dirt");}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.Barrel")
public final class BarrelCT {

    @ZenCodeType.Method
    public static void addItem(final String base, final String item, final String result) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD,
                recipe(base, "item", item, result));
    }

    @ZenCodeType.Method
    public static void addFluid(final String base, final String fluid, final String result) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD,
                recipe(base, "fluid", fluid, result));
    }

    @ZenCodeType.Method
    public static void replaceItem(final String base, final String item, final String result) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE,
                recipe(base, "item", item, result));
    }

    @ZenCodeType.Method
    public static void replaceFluid(final String base, final String fluid, final String result) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE,
                recipe(base, "fluid", fluid, result));
    }

    @ZenCodeType.Method
    public static void removeItem(final String base, final String item) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE,
                target(base, "item", item));
    }

    @ZenCodeType.Method
    public static void removeFluid(final String base, final String fluid) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE,
                target(base, "fluid", fluid));
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void replaceJson(final String json) {
        CraftTweakerBridge.submit(TableId.BARREL, CustomizationOverlay.Action.REPLACE,
                CraftTweakerBridge.parse(json));
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
