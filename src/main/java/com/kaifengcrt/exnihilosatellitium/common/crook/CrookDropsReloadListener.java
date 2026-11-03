package com.kaifengcrt.exnihilosatellitium.common.crook;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Server-side reload hook for the crook leaf tables. */
public final class CrookDropsReloadListener extends SimplePreparableReloadListener<Void> {

    private final boolean clientView;

    public CrookDropsReloadListener(final boolean clientView) {
        this.clientView = clientView;
    }

    @Override
    protected Void prepare(final ResourceManager manager, final ProfilerFiller profiler) {
        // Tag expansion needs the tag bindings, which are only ready in apply().
        return null;
    }

    @Override
    protected void apply(final Void unused, final ResourceManager manager, final ProfilerFiller profiler) {
        CrookDrops.load(manager, this.clientView);
    }
}
