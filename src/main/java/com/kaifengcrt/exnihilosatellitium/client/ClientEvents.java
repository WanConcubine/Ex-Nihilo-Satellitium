package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveReloadListener;
import com.kaifengcrt.exnihilosatellitium.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/** Client-only block entity renderers and client resource reload hooks. */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BARREL.get(), BarrelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIEVE.get(), SieveRenderer::new);
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
    }
}
