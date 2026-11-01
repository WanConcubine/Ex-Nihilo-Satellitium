package com.kaifengcrt.exnihilosatellitium.common.network;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: store the auto-output configuration of the machine at
 * {@code pos}. The whole state travels as absolute values (not a toggle) so a
 * dropped or duplicated packet can never desynchronise the GUI from the server.
 *
 * @param pos        machine block position
 * @param autoOutput master switch
 * @param mask       bit per {@code Direction#get3DDataValue()}
 */
public record MachineOutputPayload(BlockPos pos, boolean autoOutput, int mask)
        implements CustomPacketPayload {

    public static final Type<MachineOutputPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "set_machine_output"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineOutputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, MachineOutputPayload::pos,
                    ByteBufCodecs.BOOL, MachineOutputPayload::autoOutput,
                    ByteBufCodecs.VAR_INT, MachineOutputPayload::mask,
                    MachineOutputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
