package com.kaifengcrt.exnihilosatellitium.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Vanilla water sprites with a flat tint, shared by the barrel-only fluids
 * (sugar water, concentrated honey). Reusing the water texture keeps the two
 * fluids visually consistent with witch water and avoids shipping new fluid
 * atlases.
 */
public final class TintedWaterClientExtensions implements IClientFluidTypeExtensions {

    private static final ResourceLocation STILL =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation FLOWING =
            new ResourceLocation("minecraft", "block/water_flow");

    private final int tint;

    public TintedWaterClientExtensions(final int tint) {
        this.tint = tint;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return FLOWING;
    }

    @Override
    public int getTintColor() {
        return this.tint;
    }
}
