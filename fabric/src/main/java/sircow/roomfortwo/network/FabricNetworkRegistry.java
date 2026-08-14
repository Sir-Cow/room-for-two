package sircow.roomfortwo.network;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class FabricNetworkRegistry implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {}

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Constants.id("bed_occupancy_sync"), FabricNetworkRegistry::handleBedOccupancySync);
    }

    private static void handleBedOccupancySync(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        BedOccupancySyncPayload payload = new BedOccupancySyncPayload(buf);
        client.execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots()));
    }
}
