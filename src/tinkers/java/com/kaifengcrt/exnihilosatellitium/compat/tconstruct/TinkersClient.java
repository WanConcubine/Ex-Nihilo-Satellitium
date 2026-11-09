package com.kaifengcrt.exnihilosatellitium.compat.tconstruct;

import com.kaifengcrt.exnihilosatellitium.client.model.ToolModelBridge;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import slimeknights.tconstruct.library.client.model.tools.ToolModel;
import slimeknights.tconstruct.library.tools.item.IModifiable;

/**
 * Client-only wiring for the Tinkers' hammer model.
 *
 * <p>The item model uses TiC's {@code tconstruct:tool} geometry loader, which
 * composites one sprite per tool part and recolours each sprite from that
 * part's material. TiC only tints items that registered its item-colour
 * handler, so register it here for our item.</p>
 */
public final class TinkersClient {

    private TinkersClient() {
    }

    /**
     * Registered from {@code ModItems.register} only on a client that actually
     * has Tinkers installed; an {@code @EventBusSubscriber} annotation would
     * make FML load this class (and its TiC references) on every client.
     */
    public static void register(final IEventBus modBus) {
        ToolModelBridge.install((json, context) -> ToolModel.LOADER.read(json, context));
        modBus.addListener(TinkersClient::registerItemColors);
    }

    private static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        final Item item = TinkersTools.item();
        if (item instanceof IModifiable modifiable) {
            ToolModel.registerItemColors(event.getItemColors(), () -> modifiable);
        }
    }
}
