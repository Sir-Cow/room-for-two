package sircow.roomfortwo.network;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class FabricNetworkRegistry implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(BedOccupancySyncPayload.TYPE, BedOccupancySyncPayload.STREAM_CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            SleepPosCommand.register(dispatcher);
            BedCheckCommand.register(dispatcher);
        });
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BedOccupancySyncPayload.TYPE, (payload, context) -> context.client().execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots(), payload.entitySleepPositions())));
    }
}
