package sircow.roomfortwo.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import sircow.roomfortwo.util.SleepPosition;

import java.util.HashMap;
import java.util.Map;

public class BedOccupancySyncPayload implements IMessage {
    private Map<Integer, Integer> entitySlots;
    private Map<Integer, SleepPosition> entitySleepPositions;

    public BedOccupancySyncPayload() {}

    public BedOccupancySyncPayload(Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        this.entitySlots = entitySlots;
        this.entitySleepPositions = entitySleepPositions;
    }

    public Map<Integer, Integer> getEntitySlots() {
        return entitySlots;
    }

    public Map<Integer, SleepPosition> getEntitySleepPositions() {
        return entitySleepPositions;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();

        entitySlots = new HashMap<>();

        for (int i = 0; i < size; i++) {
            entitySlots.put(buf.readInt(), buf.readInt());
        }

        int sleepPosSize = buf.readInt();

        entitySleepPositions = new HashMap<>();

        for (int i = 0; i < sleepPosSize; i++) {
            int entityId = buf.readInt();
            int ordinal = buf.readInt();
            SleepPosition[] values = SleepPosition.values();

            if (ordinal >= 0 && ordinal < values.length) entitySleepPositions.put(entityId, values[ordinal]);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entitySlots.size());

        for (Map.Entry<Integer, Integer> entry : entitySlots.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeInt(entry.getValue());
        }

        buf.writeInt(entitySleepPositions.size());

        for (Map.Entry<Integer, SleepPosition> entry : entitySleepPositions.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeInt(entry.getValue().ordinal());
        }
    }
}
