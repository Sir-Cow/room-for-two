package sircow.roomfortwo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import sircow.roomfortwo.platform.services.IPlatformNetwork;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public class FabricNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots, entitySleepPositions);
        Vec3 centre = BedOccupancyTracker.bedWorldCentre(level, bedPos);

        for (ServerPlayer player : level.players()) {
            if (player.position().closerThan(centre, 128)) {
                FabricNetworkRegistry.sendBedOccupancySync(player, payload);
            }
        }
    }

    @Override
    public void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(Map.of(), entitySleepPositions);
        Vec3 centre = BedOccupancyTracker.bedWorldCentre(level, bedPos);

        for (ServerPlayer player : level.players()) {
            if (player.position().closerThan(centre, 128)) {
                FabricNetworkRegistry.sendBedOccupancySync(player, payload);
            }
        }
    }

    @Override
    public void sendBedOccupancy(ServerPlayer player, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        FabricNetworkRegistry.sendBedOccupancySync(player, new BedOccupancySyncPayload(entitySlots, entitySleepPositions));
    }
}
