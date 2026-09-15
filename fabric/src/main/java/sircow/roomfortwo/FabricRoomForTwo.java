package sircow.roomfortwo;

import net.fabricmc.api.ModInitializer;
import sircow.roomfortwo.trigger.FabricModTriggers;

public class FabricRoomForTwo implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonClass.init();
        FabricModTriggers.registerFabricModTriggers();
    }
}
