package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
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
     * The dedicated {@code exnihilosatellitium:chain_mining} module is the chain
     * switch. DE's own AOE (mining radius) module is deliberately not used: its
     * AOE logic skips blocks a pickaxe is not the right tool for, which is
     * exactly the gravel / sand our hammer table handles.
     */
    @Override
    public int chainLimit(final ItemStack stack) {
        final ModuleHost host = stack.getCapability(DECapabilities.MODULE_HOST_CAPABILITY)
                .orElse(null);
        if (host == null) {
            return 0;
        }
        final ChainData data = host.getModuleData(ChainModule.TYPE, null);
        return data == null ? 0 : Math.max(0, data.blocks());
    }

    @Override
    public int energyPerUse() {
        // DE uses -99 in its config as "keep the internal default" (256).
        return EquipCfg.energyHarvest > 0 ? EquipCfg.energyHarvest : 256;
    }
}
