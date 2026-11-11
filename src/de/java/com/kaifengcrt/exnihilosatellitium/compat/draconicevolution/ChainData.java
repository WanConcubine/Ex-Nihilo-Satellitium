package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.draconicevolution.api.modules.data.ModuleData;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleContext;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Payload of the chain module: how many extra blocks one crush may take.
 *
 * <p>DE modules carry a {@link ModuleData} so the tooltip and the module GUI can
 * describe what the module does. Multiple installed modules of the same type
 * are combined through {@link #combine(ChainData)}.</p>
 */
public record ChainData(int blocks) implements ModuleData<ChainData> {

    @Override
    public ChainData combine(final ChainData other) {
        return new ChainData(this.blocks + other.blocks);
    }

    @Override
    public void addInformation(final Map<Component, Component> map, final ModuleContext context,
                               final boolean isTooltip) {
        map.put(Component.translatable("module.exnihilosatellitium.chain_mining.blocks", blocks),
                Component.translatable("module.exnihilosatellitium.chain_mining.blocks.info"));
    }
}
