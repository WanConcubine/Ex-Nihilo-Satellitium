package com.kaifengcrt.exnihilosatellitium.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

/**
 * Narrow bridge between the always-registered {@code exnihilosatellitium:tool}
 * model loader and the optional Tinkers' Construct tool geometry.
 *
 * <p>The core loader must exist even when Tinkers is absent, otherwise a client
 * without Tinkers logs {@code Model loader 'tconstruct:tool' not found} for the
 * orphaned item model. When Tinkers is installed, its client compat installs
 * this factory so the same JSON is baked by TiC's real {@code ToolModel}
 * (per-part material sprites); without Tinkers the loader falls back to a
 * plain layered item model.</p>
 */
public final class ToolModelBridge {

    /** Reads the TiC tool geometry, backed by Tinkers' own loader. */
    @FunctionalInterface
    public interface Factory {
        IUnbakedGeometry<?> read(JsonObject json, JsonDeserializationContext context);
    }

    @Nullable
    private static volatile Factory factory;

    private ToolModelBridge() {
    }

    public static void install(final Factory value) {
        factory = value;
    }

    public static boolean available() {
        return factory != null;
    }

    public static IUnbakedGeometry<?> read(final JsonObject json,
                                           final JsonDeserializationContext context) {
        final Factory current = factory;
        if (current == null) {
            throw new IllegalStateException("Tinkers tool model factory is not installed");
        }
        return current.read(json, context);
    }
}
