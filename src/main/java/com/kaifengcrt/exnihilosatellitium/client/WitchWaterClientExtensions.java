package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Client-side look of witch water: purple still/flow textures. The textures are
 * drawn in colour, so the tint stays opaque white.
 */
public final class WitchWaterClientExtensions implements IClientFluidTypeExtensions {

    private static final ResourceLocation STILL =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "block/witch_water_still");
    private static final ResourceLocation FLOWING =
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "block/witch_water_flow");

    @Override
    public ResourceLocation getStillTexture() {
        return STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return FLOWING;
    }
}
