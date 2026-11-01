package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.item.PorcelainBucketItem;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fluids.FluidStack;

/**
 * Client-only registrations.
 */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BARREL.get(), BarrelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIEVE.get(), SieveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INFESTED_LEAVES.get(), InfestedLeavesRenderer::new);
    }

    /** Item model predicate: the porcelain bucket switches to the filled model. */
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.PORCELAIN_BUCKET.get(),
                new ResourceLocation(ExNihiloSatellitium.MOD_ID, "filled"),
                (stack, level, entity, seed) -> PorcelainBucketItem.fluidOf(stack).isEmpty() ? 0.0F : 1.0F));
    }

    /** Tint the porcelain bucket's fluid overlay with the stored fluid colour. */
    @SubscribeEvent
    public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> {
            if (index != 1) {
                return -1;
            }
            final FluidStack fluid = PorcelainBucketItem.fluidOf(stack);
            if (fluid.isEmpty()) {
                return 0xFFFFFFFF;
            }
            int tint = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
            if (tint == -1 || tint == 0xFFFFFFFF) {
                // Vanilla water/lava textures carry their own colour; the overlay is flat white.
                if (fluid.getFluid() == Fluids.WATER) {
                    tint = 0xFF3F76E4;
                } else if (fluid.getFluid() == Fluids.LAVA) {
                    tint = 0xFFFF8A2A;
                } else {
                    tint = 0xFFB0A6C4;
                }
            }
            return tint;
        }, ModItems.PORCELAIN_BUCKET.get());
    }
}
