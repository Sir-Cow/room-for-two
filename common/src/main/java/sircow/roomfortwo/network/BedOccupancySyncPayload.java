package sircow.roomfortwo.network;

import net.minecraft.network.FriendlyByteBuf;
import sircow.roomfortwo.util.SleepPosition;

import java.util.HashMap;
import java.util.Map;

public record BedOccupancySyncPayload(Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
    public BedOccupancySyncPayload(FriendlyByteBuf buf) {
        this(readSlots(buf), readSleepPositions(buf));
    }

    private static Map<Integer, Integer> readSlots(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readVarInt(), buf.readVarInt());
        }
        return map;
    }

    private static Map<Integer, SleepPosition> readSleepPositions(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, SleepPosition> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            int entityId = buf.readVarInt();
            int ordinal = buf.readVarInt();
            SleepPosition[] values = SleepPosition.values();

            if (ordinal >= 0 && ordinal < values.length) map.put(entityId, values[ordinal]);
        }
        return map;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entitySlots.size());

        this.entitySlots.forEach((entityId, slot) -> {
            buf.writeVarInt(entityId);
            buf.writeVarInt(slot);
        });
        buf.writeVarInt(this.entitySleepPositions.size());
        this.entitySleepPositions.forEach((entityId, sleepPos) -> {
            buf.writeVarInt(entityId);
            buf.writeVarInt(sleepPos.ordinal());
        });
    }
}
