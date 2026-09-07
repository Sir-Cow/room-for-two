package sircow.roomfortwo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.PacketDistributor;
import sircow.roomfortwo.platform.services.IPlatformNetwork;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public class ForgeNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots, entitySleepPositions);

        double x = bedPos.getX();
        double y = bedPos.getY();
        double z = bedPos.getZ();
        double radius = 128.0;

        PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(null, x, y, z, radius, level.dimension());
        ForgeNetworkRegistry.INSTANCE.send(payload, PacketDistributor.NEAR.with(targetPoint));
    }

    @Override
    public void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(Map.of(), entitySleepPositions);

        double x = bedPos.getX();
        double y = bedPos.getY();
        double z = bedPos.getZ();
        double radius = 128.0;

        PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(null, x, y, z, radius, level.dimension());
        ForgeNetworkRegistry.INSTANCE.send(payload, PacketDistributor.NEAR.with(targetPoint));
    }
}
