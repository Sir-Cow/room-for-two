package sircow.roomfortwo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;

@Mod(Constants.MOD_ID)
public class ForgeRoomForTwo {
    public ForgeRoomForTwo() {
        CommonClass.init();
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SleepPosCommand.register(event.getDispatcher());
        BedCheckCommand.register(event.getDispatcher());
    }
}
