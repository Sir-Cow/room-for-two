package sircow.roomfortwo;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.trigger.ModTriggers;

@Mod(Constants.MOD_ID)
public class ForgeRoomForTwo {
    public ForgeRoomForTwo() {
        CommonClass.init();
        ModTriggers.getTriggers().forEach((id, trigger) -> CriteriaTriggers.register(trigger));
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SleepPosCommand.register(event.getDispatcher());
        BedCheckCommand.register(event.getDispatcher());
    }
}
