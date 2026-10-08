package sircow.roomfortwo.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class FabricNetworkRegistry implements ModInitializer, ClientModInitializer {
    public static final ResourceLocation BED_OCCUPANCY_SYNC = Constants.id("bed_occupancy_sync");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            SleepPosCommand.register(dispatcher);
            BedCheckCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BED_OCCUPANCY_SYNC, (client, handler, buf, responseSender) -> {
            BedOccupancySyncPayload payload = new BedOccupancySyncPayload(buf);
            client.execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots(), payload.entitySleepPositions()));
        });
    }

    public static void sendBedOccupancySync(ServerPlayer player, BedOccupancySyncPayload payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buf);
        ServerPlayNetworking.send(player, BED_OCCUPANCY_SYNC, buf);
    }
}
