package com.kaifengcrt.exnihilosatellitium.common.sieve;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Reload hook: rebuilding the sift table is just a re-read of the JSON files. */
public final class SieveReloadListener extends SimplePreparableReloadListener<Void> {

    private final boolean clientView;

    public SieveReloadListener(final boolean clientView) {
        this.clientView = clientView;
    }

    @Override
    protected Void prepare(final ResourceManager manager, final ProfilerFiller profiler) {
        SieveRegistry.load(manager, this.clientView);
        return null;
    }

    @Override
    protected void apply(final Void unused, final ResourceManager manager, final ProfilerFiller profiler) {
        // Table already rebuilt in prepare().
    }
}
