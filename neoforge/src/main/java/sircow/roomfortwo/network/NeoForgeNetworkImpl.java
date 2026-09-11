package sircow.roomfortwo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import sircow.roomfortwo.platform.services.IPlatformNetwork;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public class NeoForgeNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots, entitySleepPositions);

        double x = bedPos.getX();
        double y = bedPos.getY();
        double z = bedPos.getZ();
        double radius = 128.0;

        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }

    @Override
    public void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(Map.of(), entitySleepPositions);

        double x = bedPos.getX();
        double y = bedPos.getY();
        double z = bedPos.getZ();
        double radius = 128.0;

        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }
}
