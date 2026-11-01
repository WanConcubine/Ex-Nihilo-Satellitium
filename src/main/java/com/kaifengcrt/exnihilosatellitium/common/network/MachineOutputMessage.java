package com.kaifengcrt.exnihilosatellitium.common.network;

import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client to server: store the auto-output configuration of the machine at
 * {@code pos}. The whole state travels as absolute values (not a toggle) so a
 * dropped or duplicated packet can never desynchronise the GUI from the server.
 */
public record MachineOutputMessage(BlockPos pos, boolean autoOutput, int mask) {

    public static void encode(final MachineOutputMessage message, final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos());
        buffer.writeBoolean(message.autoOutput());
        buffer.writeVarInt(message.mask());
    }

    public static MachineOutputMessage decode(final FriendlyByteBuf buffer) {
        return new MachineOutputMessage(buffer.readBlockPos(), buffer.readBoolean(),
                buffer.readVarInt());
    }

    public static void handle(final MachineOutputMessage message,
                              final Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        // Payload handlers must not touch game state outside the main thread.
        context.enqueueWork(() -> {
            final ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (!(player.level().getBlockEntity(message.pos()) instanceof MachineBlockEntity machine)) {
                return;
            }
            // Same 8 block reach as the open GUI; a stale screen cannot reconfigure
            // a machine the player already walked away from.
            if (!machine.stillValid(player)) {
                return;
            }
            machine.setOutputConfig(message.mask(), message.autoOutput());
        });
        context.setPacketHandled(true);
    }
}
