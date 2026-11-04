package com.kaifengcrt.exnihilosatellitium.common.fluid;

import com.kaifengcrt.exnihilosatellitium.client.TintedWaterClientExtensions;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

/**
 * Barrel-chain fluid type (sugar water / concentrated honey): water-like
 * handling with a per-fluid client tint. {@code ModFluids} attaches a source
 * block and a bucket, so the fluids can also be placed/collected.
 */
public final class TintedWaterFluidType extends FluidType {

    private final int tint;

    public TintedWaterFluidType(final String descriptionId, final int density, final int viscosity,
                                final int temperature, final boolean canSwim,
                                final boolean canExtinguish, final int tint) {
        super(FluidType.Properties.create()
                .descriptionId(descriptionId)
                .density(density)
                .viscosity(viscosity)
                .temperature(temperature)
                .canDrown(true)
                .canSwim(canSwim)
                .canExtinguish(canExtinguish)
                .supportsBoating(false));
        this.tint = tint;
    }

    @Override
    public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new TintedWaterClientExtensions(this.tint));
    }
}
