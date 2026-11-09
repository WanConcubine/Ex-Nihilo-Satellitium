package com.kaifengcrt.exnihilosatellitium.client.model;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers the always-available {@code exnihilosatellitium:tool} geometry
 * loader used by the Tinkers hammer model.
 */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModelLoaders {

    private ClientModelLoaders() {
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(final ModelEvent.RegisterGeometryLoaders event) {
        event.register(ToolGeometryLoader.NAME, new ToolGeometryLoader());
    }
}
