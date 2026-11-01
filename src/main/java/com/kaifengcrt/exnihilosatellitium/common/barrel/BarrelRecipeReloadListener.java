package com.kaifengcrt.exnihilosatellitium.common.barrel;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Reload hook: rebuilding the barrel transform table is just a re-read of the JSON files. */
public final class BarrelRecipeReloadListener extends SimplePreparableReloadListener<Void> {

    private final boolean clientView;

    public BarrelRecipeReloadListener(final boolean clientView) {
        this.clientView = clientView;
    }

    @Override
    protected Void prepare(final ResourceManager manager, final ProfilerFiller profiler) {
        BarrelRecipes.load(manager, this.clientView);
        return null;
    }

    @Override
    protected void apply(final Void unused, final ResourceManager manager, final ProfilerFiller profiler) {
        // Table already rebuilt in prepare().
    }
}
