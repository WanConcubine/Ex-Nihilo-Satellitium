package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.init.TechProperties;
import com.brandon3055.draconicevolution.items.equipment.DETier;
import com.brandon3055.draconicevolution.items.equipment.ModularPickaxe;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerTool;

/**
 * A Draconic Evolution modular pickaxe that also acts as an Ex Nihilo hammer.
 *
 * <p>Extending DE's pickaxe keeps the whole DE tool stack: the module grid,
 * energy storage, speed / AOE / energy modules, the module GUI and the fusion
 * data transfer. The Ex Nihilo crush table is applied by {@code ToolEvents}
 * because this class implements {@link HammerTool}; durability wear is skipped
 * for it so DE's energy system stays the only cost.</p>
 */
public final class DEHammerItem extends ModularPickaxe implements HammerTool {

    private final int tier;

    public DEHammerItem(final TechLevel level, final int tier) {
        super(itemTier(level), properties(level));
        this.tier = tier;
    }

    private static DETier itemTier(final TechLevel level) {
        return switch (level) {
            case WYVERN -> DEContent.WYVERN_TIER;
            case DRACONIC -> DEContent.DRACONIC_TIER;
            case CHAOTIC -> DEContent.CHAOTIC_TIER;
            default -> new DETier(level);
        };
    }

    private static TechProperties properties(final TechLevel level) {
        return switch (level) {
            case WYVERN -> DEContent.WYVERN_TOOLS;
            case DRACONIC -> DEContent.DRACONIC_TOOLS;
            case CHAOTIC -> DEContent.CHAOTIC_TOOLS;
            default -> new TechProperties(level);
        };
    }

    @Override
    public int tier() {
        return this.tier;
    }
}
