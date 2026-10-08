package sircow.roomfortwo.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public interface IPlatformNetwork {
    void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions);
    void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions);
}
