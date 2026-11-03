package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.command.ExNihiloTablesCommand;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDropsReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.table.Tables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Forge-bus events that are not tied to a single block.
 */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent event) {
        int ours = 0;
        for (final var holder : event.getServer().getRecipeManager().getRecipes()) {
            if (holder.id().getNamespace().equals(ExNihiloSatellitium.MOD_ID)) {
                ours++;
            }
        }
        int compostable = 0;
        for (final var item : BuiltInRegistries.ITEM) {
            if (new ItemStack(item).is(com.kaifengcrt.exnihilosatellitium.common.tag.ModTags.Items.COMPOSTABLE)) {
                compostable++;
            }
        }
        ExNihiloSatellitium.LOGGER.info("[debug] our recipes={} compostable items={}", ours, compostable);
    }

    /**
     * Tags are bound after every reload listener has run, so the tables that
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
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        ExNihiloTablesCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new CompostReloadListener(false));
        event.addListener(new SieveReloadListener(false));
        event.addListener(new BarrelRecipeReloadListener(false));
        event.addListener(new HammerRecipeReloadListener(false));
        event.addListener(new CrookDropsReloadListener(false));
        event.addListener(new CrucibleReloadListener(false));
    }
}
