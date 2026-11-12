package sircow.roomfortwo.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import sircow.roomfortwo.util.BedOccupancyTracker;

public final class RoomForTwoNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("roomfortwo");
    private static int packetId;

    public static void init() {
        INSTANCE.registerMessage(BedOccupancySyncHandler.class, BedOccupancySyncPayload.class, packetId++, Side.CLIENT);
    }

    public static void sendToAllTracking(BedOccupancySyncPayload packet, BlockPos pos, int dimension) {
        Vec3d centre = BedOccupancyTracker.bedWorldCentre(pos);
        INSTANCE.sendToAllAround(packet, new NetworkRegistry.TargetPoint(dimension, centre.x, centre.y, centre.z, 128.0D));
    }

    public static void sendToPlayer(BedOccupancySyncPayload packet, EntityPlayerMP player) {
        INSTANCE.sendTo(packet, player);
    }
}
