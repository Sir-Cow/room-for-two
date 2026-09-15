package sircow.roomfortwo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import sircow.roomfortwo.platform.services.IPlatformNetwork;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public class FabricNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots, entitySleepPositions);

        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(bedPos, 128)) {
                FabricNetworkRegistry.sendBedOccupancySync(player, payload);
            }
        }
    }

    @Override
    public void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(Map.of(), entitySleepPositions);

        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(bedPos, 128)) {
                FabricNetworkRegistry.sendBedOccupancySync(player, payload);
            }
        }
    }
}
