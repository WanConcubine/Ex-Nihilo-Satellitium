package com.kaifengcrt.exnihilosatellitium.common.network;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * P4 S2: the effective tables, sent from the server on login and after every
 * reload. The body is the {@code TableSnapshot} JSON; the client rejects a
 * different schema instead of applying a stale shape.
 */
public record TableSnapshotPayload(String json) implements CustomPacketPayload {

    /** One MiB is ~40x the measured snapshot; the schema guard catches the rest. */
    private static final int MAX_CHARS = 1 << 20;

    public static final Type<TableSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExNihiloSatellitium.MOD_ID, "table_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, TableSnapshotPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUtf(payload.json(), MAX_CHARS),
            buffer -> new TableSnapshotPayload(buffer.readUtf(MAX_CHARS)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
