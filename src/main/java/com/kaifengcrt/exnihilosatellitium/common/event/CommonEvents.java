package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.command.ExNihiloTablesCommand;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDropsReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.Tables;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-bus events that are not tied to a single block.
 */
@Mod.EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        // CraftTweaker scripts execute inside this reload, after the listener
        // event; drop their previous overlay contributions so /reload replaces
        // instead of stacking.
        final int staleCt = CustomizationOverlay.clear("crafttweaker");
        if (staleCt > 0) {
            ExNihiloSatellitium.LOGGER.info(
                    "[customize] cleared {} stale crafttweaker op(s)", staleCt);
        }
        event.addListener(new BarrelRecipeReloadListener(false));
        event.addListener(new CompostReloadListener(false));
        event.addListener(new SieveReloadListener(false));
        event.addListener(new HammerRecipeReloadListener(false));
        event.addListener(new CrookDropsReloadListener(false));
        event.addListener(new CrucibleReloadListener(false));
    }

    /**
     * Tags are bound after every reload listener has run, so tables that
     * accept {@code #tag} inputs rebuild here. On an integrated server the
     * client-packet event is skipped: the static registries are already the
     * server's.
     */
    @SubscribeEvent
    public static void onTagsUpdated(final TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            rebuildAll(false);
        } else if (event.shouldUpdateStaticData()) {
            rebuildAll(true);
        }
    }

    private static void rebuildAll(final boolean clientView) {
        Tables.rebuildAll(clientView);
    }

    @SubscribeEvent
    public static void onServerStopped(final ServerStoppedEvent event) {
        final int dropped = CustomizationOverlay.clearAll();
        if (dropped > 0) {
            ExNihiloSatellitium.LOGGER.info(
                    "[customize] cleared {} overlay op(s) on server stop", dropped);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        ExNihiloTablesCommand.register(event.getDispatcher());
    }
}
