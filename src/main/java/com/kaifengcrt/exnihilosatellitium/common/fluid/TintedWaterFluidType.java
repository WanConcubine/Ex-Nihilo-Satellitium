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

    public TintedWaterFluidType(final String descriptionId, final int density, final int viscosity,
                                final int temperature, final boolean canSwim,
                                final boolean canExtinguish) {
        super(FluidType.Properties.create()
                .descriptionId(descriptionId)
                .density(density)
                .viscosity(viscosity)
                .temperature(temperature)
                .canDrown(true)
                .canSwim(canSwim)
                .canExtinguish(canExtinguish)
                .supportsBoating(false));
    }

    /**
     * {@link FluidType}'s constructor calls this before our own fields are
     * initialized, so the tint must be derived from the description id that the
     * super constructor has already stored. Passing a field here would hand the
     * client a 0 (black) tint.
     */
    @Override
    public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new TintedWaterClientExtensions(tintFor(this.getDescriptionId())));
    }

    private static int tintFor(final String descriptionId) {
        return switch (descriptionId) {
            case "fluid_type.exnihilosatellitium.sugar_water" -> 0xFFE8C97A;
            case "fluid_type.exnihilosatellitium.concentrated_honey" -> 0xFFD98A2B;
            default -> 0xFFFFFFFF;
        };
    }
}
