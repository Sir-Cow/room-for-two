package sircow.roomfortwo.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Collections;
import java.util.List;

public class SleepPosCommand extends CommandBase {
    private static final String[] POSITIONS = {"left", "right", "front", "back", "none"};

    @Override
    public @NotNull String getName() {
        return "sleeppos";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "commands.roomfortwo.sleeppos.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(@NotNull MinecraftServer server, @NotNull ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        if (args.length == 0) {
            SleepPosition current = BedOccupancyTracker.getServerSleepPos(player.getEntityId());
            sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.sleeppos.query", current.getName()));
            return;
        }

        SleepPosition position = SleepPosition.fromString(args[0]);

        if (position == null) {
            sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.sleeppos.invalid", args[0]));
            return;
        }

        BedOccupancyTracker.setServerSleepPos(player.getEntityId(), position);
        sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.sleeppos.set", position.getName()));
    }

    @Override
    public @NotNull List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, POSITIONS);
        }
        return Collections.emptyList();
    }
}
