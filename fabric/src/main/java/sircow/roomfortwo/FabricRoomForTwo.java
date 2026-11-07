package sircow.roomfortwo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import sircow.roomfortwo.trigger.FabricModTriggers;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class FabricRoomForTwo implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonClass.init();
        FabricModTriggers.registerFabricModTriggers();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> BedOccupancyTracker.syncToPlayer(handler.player));
    }
}
