package com.kaifengcrt.exnihilosatellitium.common.fluid;

import com.kaifengcrt.exnihilosatellitium.client.WitchWaterClientExtensions;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

/**
 * Witch water fluid type (M6): slightly denser and more viscous than water so a
 * misplaced bucket does not flood the island. The client look (vanilla water
 * textures tinted purple) is attached in {@link #initializeClient}; that is the
 * 1.20.1 hook (1.21 uses a {@code RegisterClientExtensionsEvent} instead).
 */
public class WitchWaterFluidType extends FluidType {

    public WitchWaterFluidType() {
        super(FluidType.Properties.create()
                .descriptionId("fluid_type.exnihilosatellitium.witch_water")
                .density(1100)
                .viscosity(1500)
                .temperature(300)
                .canDrown(true)
                .canSwim(true)
                .canExtinguish(true)
                .supportsBoating(false));
    }

    @Override
    public void initializeClient(final Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new WitchWaterClientExtensions());
    }
}
