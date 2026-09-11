package sircow.roomfortwo.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.util.SleepPosition;

import java.util.HashMap;
import java.util.Map;

public record BedOccupancySyncPayload(Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) implements CustomPacketPayload {
    public static final Type<BedOccupancySyncPayload> TYPE = new Type<>(Constants.id("bed_occupancy_sync"));

    public static final StreamCodec<FriendlyByteBuf, BedOccupancySyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entitySlots().size());
                payload.entitySlots().forEach((entityId, slot) -> {
                    buf.writeVarInt(entityId);
                    buf.writeVarInt(slot);
                });
                buf.writeVarInt(payload.entitySleepPositions().size());
                payload.entitySleepPositions().forEach((entityId, sleepPos) -> {
                    buf.writeVarInt(entityId);
                    buf.writeVarInt(sleepPos.ordinal());
                });
            },
            buf -> {
                int size = buf.readVarInt();
                Map<Integer, Integer> slotMap = new HashMap<>();

                for (int i = 0; i < size; i++) {
                    slotMap.put(buf.readVarInt(), buf.readVarInt());
                }

                int sleepPosSize = buf.readVarInt();
                Map<Integer, SleepPosition> sleepPosMap = new HashMap<>();

                for (int i = 0; i < sleepPosSize; i++) {
                    int entityId = buf.readVarInt();
                    int ordinal = buf.readVarInt();
                    SleepPosition[] values = SleepPosition.values();

                    if (ordinal >= 0 && ordinal < values.length) sleepPosMap.put(entityId, values[ordinal]);
                }
                return new BedOccupancySyncPayload(slotMap, sleepPosMap);
            }
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
