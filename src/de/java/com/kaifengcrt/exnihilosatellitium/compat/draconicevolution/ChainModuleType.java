package com.kaifengcrt.exnihilosatellitium.compat.draconicevolution;

import com.brandon3055.draconicevolution.api.modules.Module;
import com.brandon3055.draconicevolution.api.modules.ModuleCategory;
import com.brandon3055.draconicevolution.api.modules.ModuleType;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleEntity;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

/**
 * The {@code chain_mining} module type.
 *
 * <p>Implemented directly instead of using {@code ModuleTypeImpl}: that class
 * hardcodes the display-name key to {{@code module_type.draconicevolution.*}},
 * and this is our own module living in our namespace. The category is
 * {@code MINING_TOOL}, the category DE adds to any {@code IModularMiningTool}
 * host (our modular hammers included), so the module installs on them.</p>
 */
public final class ChainModuleType implements ModuleType<ChainData> {

    public static final String NAME = "chain_mining";

    @Override
    public int getDefaultWidth() {
        return 1;
    }

    @Override
    public int getDefaultHeight() {
        return 1;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("module_type.exnihilosatellitium.chain_mining");
    }

    @Override
    public ModuleEntity<ChainData> createEntity(final Module<ChainData> module) {
        return new ModuleEntity<>(module);
    }

    @Override
    public Collection<ModuleCategory> getCategories() {
        return List.of(ModuleCategory.MINING_TOOL);
    }
}
