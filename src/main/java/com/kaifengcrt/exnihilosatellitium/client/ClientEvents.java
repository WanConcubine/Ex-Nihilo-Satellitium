package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDropsReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveReloadListener;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import com.kaifengcrt.exnihilosatellitium.registry.ModFluids;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.kaifengcrt.exnihilosatellitium.common.item.PorcelainBucketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Client-only block entity renderers and client resource reload hooks. */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BARREL.get(), BarrelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIEVE.get(), SieveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
    }

    /** Item model predicate: the porcelain bucket switches to the filled model. */
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.PORCELAIN_BUCKET.get(),
                ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "filled"),
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
                if (fluid.is(Fluids.WATER)) {
                    tint = 0xFF3F76E4;
                } else if (fluid.is(Fluids.LAVA)) {
                    tint = 0xFFFF8A2A;
                } else {
                    tint = 0xFFB0A6C4;
                }
            }
            return tint;
        }, ModItems.PORCELAIN_BUCKET.get());
    }

    /** Witch water needs explicit still/flowing textures on the client. */
    @SubscribeEvent
    public static void registerClientExtensions(final RegisterClientExtensionsEvent event) {
        event.registerFluidType(new WitchWaterClientExtensions(), ModFluids.WITCH_WATER_TYPE);
    }

    /**
     * The server-side listener only runs on the server's resource manager. Read the
     * same JSON from the client's packs as well so JEI also works when connected to
     * a dedicated server (where the client never loads the server datapacks).
     */
    @SubscribeEvent
    public static void registerReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SieveReloadListener(true));
        event.registerReloadListener(new BarrelRecipeReloadListener(true));
        event.registerReloadListener(new CompostReloadListener(true));
        event.registerReloadListener(new CrucibleReloadListener(true));
        event.registerReloadListener(new HammerRecipeReloadListener(true));
        event.registerReloadListener(new CrookDropsReloadListener(true));
    }
}
