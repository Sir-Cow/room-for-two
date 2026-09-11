package sircow.roomfortwo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;

@Mod(Constants.MOD_ID)
public class NeoForgeRoomForTwo {
    public NeoForgeRoomForTwo(IEventBus eventBus) {
        CommonClass.init();
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SleepPosCommand.register(event.getDispatcher());
        BedCheckCommand.register(event.getDispatcher());
    }
}
