package com.kaifengcrt.exnihilosatellitium.common.crucible;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Reload hook for the crucible melt table and heat-source table. */
public final class CrucibleReloadListener extends SimplePreparableReloadListener<Void> {

    private final boolean clientView;

    public CrucibleReloadListener(final boolean clientView) {
        this.clientView = clientView;
    }

    @Override
    protected Void prepare(final ResourceManager manager, final ProfilerFiller profiler) {
        // Tag expansion needs the tag bindings, which are only ready in apply().
        return null;
    }

    @Override
    protected void apply(final Void unused, final ResourceManager manager, final ProfilerFiller profiler) {
        CrucibleRecipes.load(manager, this.clientView);
        CrucibleHeat.load(manager, this.clientView);
    }
}
