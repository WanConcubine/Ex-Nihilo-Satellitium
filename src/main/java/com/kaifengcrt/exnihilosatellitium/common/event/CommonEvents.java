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
import com.kaifengcrt.exnihilosatellitium.common.network.ModNetwork;
import com.kaifengcrt.exnihilosatellitium.common.network.TableSnapshotPayload;
import com.kaifengcrt.exnihilosatellitium.common.table.TableDump;
import com.kaifengcrt.exnihilosatellitium.common.table.TableId;
import com.kaifengcrt.exnihilosatellitium.common.table.TableSnapshot;
import com.kaifengcrt.exnihilosatellitium.common.table.Tables;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Forge-bus events that are not tied to a single block.
 */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID)
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
        // KubeJS scripts run before this event; apply their queued overlay ops now.
        Tables.rebuildAll(false);

        // P4 S1/S2 self-check: capture -> codec -> apply into the client view, then
        // compare the rendered lines with the server's. On an integrated server the
        // client shares the static tables, so only a dedicated server runs the apply.
        final Map<TableId, List<String>> expected = new EnumMap<>(TableId.class);
        for (final TableId table : TableId.values()) {
            expected.put(table, TableDump.lines(table));
        }
        final TableSnapshot snapshot = TableSnapshot.capture();
        final List<String> problems = snapshot.checkCounts();
        final String json = snapshot.toJson().toString();
        final TableSnapshotPayload payload = new TableSnapshotPayload(json);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        TableSnapshotPayload.STREAM_CODEC.encode(buffer, payload);
        if (!json.equals(TableSnapshotPayload.STREAM_CODEC.decode(buffer).json())) {
            problems.add("payload codec round-trip changed the snapshot");
        }
        int compared = 0;
        if (event.getServer().isDedicatedServer()) {
            snapshot.applyClientView();
            for (final TableId table : TableId.values()) {
                final List<String> actual = TableDump.lines(table, true);
                if (!expected.get(table).equals(actual)) {
                    problems.add(table.id() + ": client view has " + actual.size()
                            + " line(s), server has " + expected.get(table).size());
                }
                compared += actual.size();
            }
        }
        if (problems.isEmpty()) {
            ExNihiloSatellitium.LOGGER.info(
                    "[snapshot] selftest OK: {} file(s), {} chars, codec ok, {} line(s) identical (schema {})",
                    snapshot.fileCount(), json.length(), compared, TableSnapshot.SCHEMA);
        } else {
            ExNihiloSatellitium.LOGGER.error("[snapshot] selftest mismatch: {}", problems);
        }
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

    /**
     * Overlay operations are not persisted on purpose, but they live in a static
     * queue: without this, leaving a single-player world would carry command/script
     * operations into the next world in the same JVM. Scripts re-submit on load.
     */
    /**
     * P4 S2: datapacks sync on join and on {@code /reload}. NeoForge fires this
     * only once a server packet listener exists, unlike {@code TagsUpdatedEvent},
     * which also runs during world load before the server is up.
     */
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ModNetwork.sendSnapshot(event.getPlayer());
        } else {
            ModNetwork.broadcastSnapshot();
        }
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

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        // CraftTweaker scripts execute inside this reload, after the listener
        // event; drop their previous overlay contributions here so /reload
        // replaces instead of stacking. KJS loads earlier and clears itself.
        final int staleCt = CustomizationOverlay.clear("crafttweaker");
        if (staleCt > 0) {
            ExNihiloSatellitium.LOGGER.info(
                    "[customize] cleared {} stale crafttweaker op(s)", staleCt);
        }
        event.addListener(new CompostReloadListener(false));
        event.addListener(new SieveReloadListener(false));
        event.addListener(new BarrelRecipeReloadListener(false));
        event.addListener(new HammerRecipeReloadListener(false));
        event.addListener(new CrookDropsReloadListener(false));
        event.addListener(new CrucibleReloadListener(false));
    }
}
