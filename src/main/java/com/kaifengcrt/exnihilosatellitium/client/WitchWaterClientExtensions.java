package com.kaifengcrt.exnihilosatellitium.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Client-side look of witch water: the vanilla water still/flow textures (they
 * are greyscale and get tinted at runtime) multiplied by a witch-water purple.
 * This keeps the animated vanilla water look instead of a hand-drawn still.
 */
public final class WitchWaterClientExtensions implements IClientFluidTypeExtensions {

    /** ARGB tint applied to the greyscale vanilla water textures. */
    private static final int TINT = 0xFF6B44A8;

    private static final ResourceLocation STILL =
            ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation FLOWING =
            ResourceLocation.withDefaultNamespace("block/water_flow");

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
        return TINT;
    }
}
