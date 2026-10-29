package com.kaifengcrt.exnihilosatellitium.client;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Debug-only: opens {@link OreDebugScreen}, grabs a screenshot, then closes it. */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, value = Dist.CLIENT)
public final class OreDebugScreenshot {

    private static final boolean ENABLED = Boolean.getBoolean("exnihilo.debugScreenshot");
    private static int ticks;

    private OreDebugScreenshot() {
    }

    @SubscribeEvent
    public static void tick(final ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        ticks++;
        if (ticks == 120) {
            mc.setScreen(new OreDebugScreen());
        } else if (ticks == 140) {
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), message -> { });
            ExNihiloSatellitium.LOGGER.info("[debug-shot] captured");
        } else if (ticks == 160) {
            mc.setScreen(null);
        }
    }
}
