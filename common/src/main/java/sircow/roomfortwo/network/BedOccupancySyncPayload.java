package sircow.roomfortwo.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public record BedOccupancySyncPayload(Map<Integer, Integer> entitySlots) {
    public BedOccupancySyncPayload(FriendlyByteBuf buf) {
        this(decode(buf));
    }

    private static Map<Integer, Integer> decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readVarInt(), buf.readVarInt());
        }
        return map;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entitySlots().size());
        entitySlots().forEach((entityId, slot) -> {
            buf.writeVarInt(entityId);
            buf.writeVarInt(slot);
        });
    }
}
