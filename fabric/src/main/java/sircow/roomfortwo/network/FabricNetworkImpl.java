package sircow.roomfortwo.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.platform.services.IPlatformNetwork;

import java.util.Map;

public class FabricNetworkImpl implements IPlatformNetwork {
    @Override
    public void broadcastBedOccupancy(ServerLevel level, BlockPos bedPos, Map<Integer, Integer> entitySlots) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(entitySlots);

        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(bedPos, 128)) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                payload.encode(buf);
                ServerPlayNetworking.send(player, Constants.id("bed_occupancy_sync"), buf);
            }
        }
    }
}
