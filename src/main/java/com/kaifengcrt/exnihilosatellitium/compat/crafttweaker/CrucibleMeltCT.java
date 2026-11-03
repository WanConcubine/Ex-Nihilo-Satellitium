package com.kaifengcrt.exnihilosatellitium.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonObject;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import org.openzen.zencode.java.ZenCodeType;

/** ZenScript: {@code mods.exnihilosatellitium.CrucibleMelt.add("minecraft:cobblestone", "minecraft:lava", 250);}. */
@ZenRegister
@ZenCodeType.Name("mods.exnihilosatellitium.CrucibleMelt")
public final class CrucibleMeltCT {

    @ZenCodeType.Method
    public static void add(final String input, final String fluid, final int amount) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.ADD,
                melt(input, fluid, amount));
    }

    @ZenCodeType.Method
    public static void replace(final String input, final String fluid, final int amount) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REPLACE,
                melt(input, fluid, amount));
    }

    @ZenCodeType.Method
    public static void remove(final String input) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("remove", true);
        CraftTweakerBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REMOVE, root);
    }

    @ZenCodeType.Method
    public static void addJson(final String json) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.ADD,
                CraftTweakerBridge.parse(json));
    }

    @ZenCodeType.Method
    public static void removeJson(final String json) {
        CraftTweakerBridge.submit(TableId.CRUCIBLE_MELT, CustomizationOverlay.Action.REMOVE,
                CraftTweakerBridge.parse(json));
    }

    private static JsonObject melt(final String input, final String fluid, final int amount) {
        final JsonObject root = CraftTweakerBridge.input(input);
        root.addProperty("fluid", fluid);
        root.addProperty("amount", amount);
        root.addProperty("solid", amount);
        return root;
    }
}
