package com.kaifengcrt.exnihilosatellitium.platform;

import com.kaifengcrt.exnihilosatellitium.common.platform.ModProbe;
import net.minecraftforge.fml.ModList;

/**
 * Forge implementation of the core {@link ModProbe}.
 *
 * <p>{@code ModList.get()} can be {@code null} very early during loading; both
 * methods treat that as "nothing loaded" instead of throwing.</p>
 */
public final class ForgeModProbe implements ModProbe {

    @Override
    public boolean isLoaded(final String modId) {
        final ModList list = ModList.get();
        return list != null && list.isLoaded(modId);
    }

    @Override
    public String version(final String modId) {
        final ModList list = ModList.get();
        if (list == null) {
            return "";
        }
        return list.getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("");
    }
}
