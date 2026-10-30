package com.kaifengcrt.exnihilosatellitium.common.hammer;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Server-side reload hook for the hammer table. */
public final class HammerRecipeReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    protected Void prepare(final ResourceManager manager, final ProfilerFiller profiler) {
        HammerRecipes.load(manager);
        return null;
    }

    @Override
    protected void apply(final Void unused, final ResourceManager manager, final ProfilerFiller profiler) {
        // Table already rebuilt in prepare().
    }
}
