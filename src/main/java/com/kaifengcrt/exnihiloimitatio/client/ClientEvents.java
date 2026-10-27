package com.kaifengcrt.exnihiloimitatio.client;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import com.kaifengcrt.exnihiloimitatio.registry.ModBlockEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only registrations.
 */
@Mod.EventBusSubscriber(modid = ExNihiloImitatio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BARREL.get(), BarrelRenderer::new);
    }
}
