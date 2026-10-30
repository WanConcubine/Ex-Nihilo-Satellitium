package com.kaifengcrt.exnihilosatellitium.common.compost;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Reads {@code data/<namespace>/compost/*.json} and feeds
 * {@link CompostRegistry}.
 */
public final class CompostReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    private final boolean clientView;

    public CompostReloadListener(final boolean clientView) {
        super(GSON, "compost");
        this.clientView = clientView;
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsons,
                         final ResourceManager resourceManager,
                         final ProfilerFiller profiler) {
        CompostRegistry.reload(jsons, this.clientView);
    }
}
