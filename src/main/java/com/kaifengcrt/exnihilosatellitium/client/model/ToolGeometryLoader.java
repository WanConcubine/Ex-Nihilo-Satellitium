package com.kaifengcrt.exnihilosatellitium.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

/**
 * Loader for {@code exnihilosatellitium:tool}.
 *
 * <p>With Tinkers installed the JSON is handed to TiC's {@code tconstruct:tool}
 * loader (material-tinted part sprites). Without it, the same JSON's
 * {@code layer0}/{@code layer1} entries are baked as a plain layered item, so
 * the orphaned model never produces an unknown-loader error.</p>
 */
public final class ToolGeometryLoader implements IGeometryLoader<ToolGeometry> {

    public static final String NAME = "tool";

    @Override
    public ToolGeometry read(final JsonObject json, final JsonDeserializationContext context) {
        if (ToolModelBridge.available()) {
            return new ToolGeometry(ToolModelBridge.read(json, context));
        }
        final IUnbakedGeometry<?> fallback = ItemLayerModel.Loader.INSTANCE.read(json, context);
        return new ToolGeometry(fallback);
    }
}
