package sircow.roomfortwo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import sircow.roomfortwo.platform.services.IPlatformNetwork;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Map;

public class NeoForgeNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots, entitySleepPositions);
        Vec3 centre = BedOccupancyTracker.bedWorldCentre(level, bedPos);
        double radius = 128.0;

        PacketDistributor.sendToPlayersNear(level, null, centre.x, centre.y, centre.z, radius, payload);
    }

    @Override
    public void broadcastSleepPosition(ServerLevel level, BlockPos bedPos, Map<Integer, SleepPosition> entitySleepPositions) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(Map.of(), entitySleepPositions);
        Vec3 centre = BedOccupancyTracker.bedWorldCentre(level, bedPos);
        double radius = 128.0;

        PacketDistributor.sendToPlayersNear(level, null, centre.x, centre.y, centre.z, radius, payload);
    }

    @Override
    public void sendBedOccupancy(ServerPlayer player, Map<Integer, Integer> entitySlots, Map<Integer, SleepPosition> entitySleepPositions) {
        PacketDistributor.sendToPlayer(player, new BedOccupancySyncPayload(entitySlots, entitySleepPositions));
    }
}
