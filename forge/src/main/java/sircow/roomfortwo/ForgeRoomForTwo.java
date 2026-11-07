package sircow.roomfortwo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mod(Constants.MOD_ID)
public class ForgeRoomForTwo {
    public ForgeRoomForTwo() {
        CommonClass.init();
        RegisterCommandsEvent.BUS.addListener(this::onRegisterCommands);
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(this::onPlayerLoggedIn);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SleepPosCommand.register(event.getDispatcher());
        BedCheckCommand.register(event.getDispatcher());
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) BedOccupancyTracker.syncToPlayer(player);
    }
}
