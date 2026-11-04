package com.kaifengcrt.exnihilosatellitium.common.event;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.barrel.BarrelRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.command.ExNihiloTablesCommand;
import com.kaifengcrt.exnihilosatellitium.common.compost.CompostReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crook.CrookDropsReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.crucible.CrucibleReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.hammer.HammerRecipeReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.item.BrainPlatinumItem;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveReloadListener;
import com.kaifengcrt.exnihilosatellitium.common.table.CustomizationOverlay;
import com.kaifengcrt.exnihilosatellitium.common.table.Tables;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

    /**
     * Brain Platinum has to win over the vanilla villager trade screen. This
     * event fires before the entity's own interaction, so canceling it reaches
     * the item without requiring the player to sneak (vanilla only falls
     * through to {@code interactLivingEntity} while Shift is held).
     */
    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)
                || !event.getItemStack().is(ModItems.BRAIN_PLATINUM.get())) {
            return;
        }
        if (BrainPlatinumItem.tryConvert(event.getEntity(), villager,
                event.getEntity().getItemInHand(event.getHand()))) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
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
