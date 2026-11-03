package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.Sieve.add("minecraft:sand", "minecraft:diamond", 0.02);}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.Sieve")
public final class SieveCT {

    @ZenCodeType.Method
    public static void add(final String input, final String output, final double chance) {
        add(input, output, chance, 1, 1);
    }

    @ZenCodeType.Method
    public static void add(final String input, final String output, final double chance,
                           final int min, final int max) {
        final JsonObject root = CraftTweakerBridge.input(input);
        final JsonArray outputs = CraftTweakerBridge.drops(
                CraftTweakerBridge.drop(output, chance, min, max));
        root.add("outputs", outputs);
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.ADD, root);
    }

    @ZenCodeType.Method
    public static void remove(final String input, final String output) {
        final JsonObject root = CraftTweakerBridge.input(input);
        final JsonArray remove = new JsonArray();
        remove.add(output);
        root.add("remove", remove);
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void removeInput(final String input) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("remove", true);
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void replace(final String input, final String output, final double chance) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.add("outputs", CraftTweakerBridge.drops(CraftTweakerBridge.drop(output, chance, 1, 1)));
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REPLACE, root);
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void replaceJson(final String json) {
        CraftTweakerBridge.submit(TableId.SIEVE, CustomizationOverlay.Action.REPLACE,
                CraftTweakerBridge.parse(json));
    }
}
