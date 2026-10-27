package com.kaifengcrt.exnihiloimitatio.common.event;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import com.kaifengcrt.exnihiloimitatio.common.compost.CompostReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-bus events that are not tied to a single block.
 */
@Mod.EventBusSubscriber(modid = ExNihiloImitatio.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new CompostReloadListener());
    }
}
