package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.AOEData;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.init.EquipCfg;
import com.brandon3055.draconicevolution.init.TechProperties;
import com.brandon3055.draconicevolution.items.equipment.DETier;
import com.brandon3055.draconicevolution.items.equipment.ModularPickaxe;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerTool;
import net.minecraft.world.item.ItemStack;

/**
 * A Draconic Evolution modular pickaxe that also acts as an Ex Nihilo hammer.
 *
 * <p>Extending DE's pickaxe keeps the module grid, energy storage, speed / AOE
 * modules and the module GUI. The crush table is applied by {@code ToolEvents}
 * because this class implements {@link HammerTool}; durability wear is skipped
 * so DE's energy system is the cost.</p>
 */
public final class DEHammerItem extends ModularPickaxe implements HammerTool {

    /** Connected blocks per AOE module step (DE reports 1 / 2 / 3). */
    private static final int CHAIN_BLOCKS_PER_AOE = 16;

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

    /**
     * DE has no "chain" module; its AOE module is the mining-radius module, so
     * it doubles as the chain switch. DE's own AOE logic skips blocks the tool
     * is not the right tool for (gravel / sand on a pickaxe), which is exactly
     * where our hammer table takes over.
     */
    @Override
    public int chainLimit(final ItemStack stack) {
        final ModuleHost host = stack.getCapability(DECapabilities.MODULE_HOST_CAPABILITY)
                .orElse(null);
        if (host == null) {
            return 0;
        }
        final AOEData aoe = host.getModuleData(ModuleTypes.AOE, new AOEData(0));
        return Math.max(0, aoe.aoe()) * CHAIN_BLOCKS_PER_AOE;
    }

    @Override
    public int energyPerUse() {
        // DE uses -99 in its config as "keep the internal default" (256).
        return EquipCfg.energyHarvest > 0 ? EquipCfg.energyHarvest : 256;
    }
}
