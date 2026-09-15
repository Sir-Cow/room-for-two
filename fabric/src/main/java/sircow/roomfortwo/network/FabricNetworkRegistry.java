package sircow.roomfortwo.network;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class FabricNetworkRegistry implements ModInitializer, ClientModInitializer {
    public static final PacketType<BedOccupancySyncPacket> BED_OCCUPANCY_SYNC = PacketType.create(
            Constants.id("bed_occupancy_sync"),
            BedOccupancySyncPacket::new
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            SleepPosCommand.register(dispatcher);
            BedCheckCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BED_OCCUPANCY_SYNC, (packet, player, responseSender) -> BedOccupancyTracker.updateClientCache(packet.payload.entitySlots(), packet.payload.entitySleepPositions()));
    }

    public static void sendBedOccupancySync(ServerPlayer player, BedOccupancySyncPayload payload) {
        ServerPlayNetworking.send(player, new BedOccupancySyncPacket(payload));
    }

    public static class BedOccupancySyncPacket implements FabricPacket {
        private final BedOccupancySyncPayload payload;

        public BedOccupancySyncPacket(FriendlyByteBuf buf) {
            this.payload = new BedOccupancySyncPayload(buf);
        }

        public BedOccupancySyncPacket(BedOccupancySyncPayload payload) {
            this.payload = payload;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            payload.write(buf);
        }

        @Override
        public PacketType<?> getType() {
            return BED_OCCUPANCY_SYNC;
        }
    }
}
