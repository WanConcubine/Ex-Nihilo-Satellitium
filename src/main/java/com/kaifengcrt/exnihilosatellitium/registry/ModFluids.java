package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.fluid.TintedWaterFluidType;
import com.kaifengcrt.exnihilosatellitium.common.fluid.WitchWaterFluidType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Fluids (M6). Witch water is a still/flowing pair plus a {@link FluidType};
 * the still fluid id is {@code exnihilosatellitium:witch_water}, which is what
 * barrel rules and bucket contents use.
 */
public final class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ExNihiloSatellitium.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ExNihiloSatellitium.MOD_ID);

    public static final RegistryObject<FluidType> WITCH_WATER_TYPE =
            FLUID_TYPES.register("witch_water", WitchWaterFluidType::new);

    public static final RegistryObject<ForgeFlowingFluid.Source> WITCH_WATER =
            FLUIDS.register("witch_water", () -> new ForgeFlowingFluid.Source(witchWaterProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_WITCH_WATER =
            FLUIDS.register("flowing_witch_water", () -> new ForgeFlowingFluid.Flowing(witchWaterProperties()));

    /**
     * Sugar water and concentrated honey are barrel-only fluids: no block and
     * no bucket, so they can only be produced, stored and drawn from a barrel.
     * The still/flowing pair is still needed for the tank and JEI.
     */
    public static final RegistryObject<FluidType> SUGAR_WATER_TYPE =
            FLUID_TYPES.register("sugar_water", () -> new TintedWaterFluidType(
                    "fluid_type.exnihilosatellitium.sugar_water",
                    1050, 1200, 300, true, true, 0xFFE8C97A));

    public static final RegistryObject<ForgeFlowingFluid.Source> SUGAR_WATER =
            FLUIDS.register("sugar_water", () -> new ForgeFlowingFluid.Source(sugarWaterProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_SUGAR_WATER =
            FLUIDS.register("flowing_sugar_water", () -> new ForgeFlowingFluid.Flowing(sugarWaterProperties()));

    public static final RegistryObject<FluidType> CONCENTRATED_HONEY_TYPE =
            FLUID_TYPES.register("concentrated_honey", () -> new TintedWaterFluidType(
                    "fluid_type.exnihilosatellitium.concentrated_honey",
                    1400, 6000, 320, false, false, 0xFFD98A2B));

    public static final RegistryObject<ForgeFlowingFluid.Source> CONCENTRATED_HONEY =
            FLUIDS.register("concentrated_honey",
                    () -> new ForgeFlowingFluid.Source(concentratedHoneyProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_CONCENTRATED_HONEY =
            FLUIDS.register("flowing_concentrated_honey",
                    () -> new ForgeFlowingFluid.Flowing(concentratedHoneyProperties()));

    private ModFluids() {
    }

    private static ForgeFlowingFluid.Properties sugarWaterProperties() {
        return barrelFluidProperties(SUGAR_WATER_TYPE, SUGAR_WATER, FLOWING_SUGAR_WATER);
    }

    private static ForgeFlowingFluid.Properties concentratedHoneyProperties() {
        return barrelFluidProperties(CONCENTRATED_HONEY_TYPE, CONCENTRATED_HONEY,
                FLOWING_CONCENTRATED_HONEY);
    }

    private static ForgeFlowingFluid.Properties barrelFluidProperties(
            final RegistryObject<FluidType> type,
            final RegistryObject<ForgeFlowingFluid.Source> still,
            final RegistryObject<ForgeFlowingFluid.Flowing> flowing) {
        return new ForgeFlowingFluid.Properties(type, still, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(3)
                .tickRate(20);
    }

    /**
     * Slower and thicker than water so it does not flood the whole island after
     * a bucket is misplaced; the block/bucket references are suppliers, so the
     * block and item registries may fill after the fluid registry.
     */
    private static ForgeFlowingFluid.Properties witchWaterProperties() {
        return new ForgeFlowingFluid.Properties(WITCH_WATER_TYPE, WITCH_WATER, FLOWING_WITCH_WATER)
                .block(ModBlocks.WITCH_WATER)
                .bucket(ModItems.WITCH_WATER_BUCKET)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .tickRate(15)
                .explosionResistance(100.0F);
    }

    public static void register(final IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }
}
