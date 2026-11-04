package com.kaifengcrt.exnihilosatellitium.registry;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Fluids (M6). Witch water is a still/flowing pair plus a {@link FluidType};
 * the still fluid id is {@code exnihilosatellitium:witch_water}, which is what
 * barrel rules and bucket contents use.
 *
 * <p>Textures and tint live on the client side in
 * {@code client.FluidClientExtensions}; without them the fluid renders with the
 * missing-texture checkerboard.</p>
 */
public final class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ExNihiloSatellitium.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, ExNihiloSatellitium.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> WITCH_WATER_TYPE =
            FLUID_TYPES.register("witch_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.exnihilosatellitium.witch_water")
                    .density(1100)
                    .viscosity(1500)
                    .temperature(300)
                    .canDrown(true)
                    .canSwim(true)
                    .canExtinguish(true)
                    .supportsBoating(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> WITCH_WATER =
            FLUIDS.register("witch_water", () -> new BaseFlowingFluid.Source(witchWaterProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_WITCH_WATER =
            FLUIDS.register("flowing_witch_water", () -> new BaseFlowingFluid.Flowing(witchWaterProperties()));

    /**
     * Sugar water and concentrated honey are barrel-chain fluids. Both have a
     * source block and a bucket so they can be transported; the barrel-specific
     * effects (delayed conversion, bee spawning) live in the block entity.
     */
    public static final DeferredHolder<FluidType, FluidType> SUGAR_WATER_TYPE =
            FLUID_TYPES.register("sugar_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.exnihilosatellitium.sugar_water")
                    .density(1050)
                    .viscosity(1200)
                    .temperature(300)
                    .canDrown(true)
                    .canSwim(true)
                    .canExtinguish(true)
                    .canConvertToSource(false)
                    .supportsBoating(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SUGAR_WATER =
            FLUIDS.register("sugar_water", () -> new BaseFlowingFluid.Source(sugarWaterProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_SUGAR_WATER =
            FLUIDS.register("flowing_sugar_water", () -> new BaseFlowingFluid.Flowing(sugarWaterProperties()));

    public static final DeferredHolder<FluidType, FluidType> CONCENTRATED_HONEY_TYPE =
            FLUID_TYPES.register("concentrated_honey", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.exnihilosatellitium.concentrated_honey")
                    .density(1400)
                    .viscosity(6000)
                    .temperature(320)
                    .canDrown(true)
                    .canSwim(false)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .supportsBoating(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> CONCENTRATED_HONEY =
            FLUIDS.register("concentrated_honey", () -> new BaseFlowingFluid.Source(
                    concentratedHoneyProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_CONCENTRATED_HONEY =
            FLUIDS.register("flowing_concentrated_honey", () -> new BaseFlowingFluid.Flowing(
                    concentratedHoneyProperties()));

    private ModFluids() {
    }

    /**
     * Properties for the two barrel-chain fluids: block + bucket are attached
     * by the per-fluid methods below; this part only sets the flow behaviour.
     */
    private static BaseFlowingFluid.Properties sugarWaterProperties() {
        return barrelFluidProperties(SUGAR_WATER_TYPE, SUGAR_WATER, FLOWING_SUGAR_WATER)
                .block(ModBlocks.SUGAR_WATER)
                .bucket(ModItems.SUGAR_WATER_BUCKET);
    }

    private static BaseFlowingFluid.Properties concentratedHoneyProperties() {
        return barrelFluidProperties(CONCENTRATED_HONEY_TYPE, CONCENTRATED_HONEY,
                FLOWING_CONCENTRATED_HONEY)
                .block(ModBlocks.CONCENTRATED_HONEY)
                .bucket(ModItems.CONCENTRATED_HONEY_BUCKET);
    }

    private static BaseFlowingFluid.Properties barrelFluidProperties(
            final DeferredHolder<FluidType, FluidType> type,
            final DeferredHolder<Fluid, BaseFlowingFluid.Source> still,
            final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing) {
        return new BaseFlowingFluid.Properties(type, still, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(3)
                .tickRate(20);
    }

    /**
     * Slower and thicker than water so it does not flood the whole island after
     * a bucket is misplaced; the block/bucket references are resolved lazily
     * because fluids are registered before blocks and items.
     */
    private static BaseFlowingFluid.Properties witchWaterProperties() {
        return new BaseFlowingFluid.Properties(WITCH_WATER_TYPE, WITCH_WATER, FLOWING_WITCH_WATER)
                .block(ModBlocks.WITCH_WATER)
                .bucket(ModItems.WITCH_WATER_BUCKET)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .tickRate(15)
                .explosionResistance(100.0F);
    }

    public static void register(final IEventBus modBus) {
        // Fluid types and fluids both register before blocks/items in NeoForge's
        // registry order, so the references above resolve in time.
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }
}
