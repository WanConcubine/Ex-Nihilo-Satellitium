package com.kaifengcrt.exnihilosatellitium.common.network;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Mod network channel. Only one client-to-server message so far: the machine
 * auto-output configuration written by {@code MachineOutputScreen}.
 */
@EventBusSubscriber(modid = ExNihiloSatellitium.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {

    private static final String VERSION = "1";

    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(VERSION).playToServer(
                MachineOutputPayload.TYPE, MachineOutputPayload.STREAM_CODEC, ModNetwork::handleOutputConfig);
    }

    private static void handleOutputConfig(final MachineOutputPayload payload,
                                           final IPayloadContext context) {
        // Payload handlers must not touch game state outside the main thread.
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.level().getBlockEntity(payload.pos()) instanceof MachineBlockEntity machine)) {
                return;
            }
            // Same 8 block reach as the open GUI; a stale screen cannot reconfigure
            // a machine the player already walked away from.
            if (!machine.stillValid(player)) {
                return;
            }
            machine.setOutputConfig(payload.mask(), payload.autoOutput());
        });
    }
}
