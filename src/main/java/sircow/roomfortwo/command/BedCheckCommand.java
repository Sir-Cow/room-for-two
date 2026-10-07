package sircow.roomfortwo.command;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import sircow.roomfortwo.api.BedOccupancyAPI;

import java.util.ArrayList;
import java.util.List;

public class BedCheckCommand extends CommandBase {
    @Override
    public @NotNull String getName() {
        return "bedcheck";
    }

    @Override
    public @NotNull String getUsage(@NotNull ICommandSender sender) {
        return "commands.roomfortwo.bedcheck.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        RayTraceResult hitResult = player.rayTrace(5.0D, 0.0F);

        if (hitResult == null || hitResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.bedcheck.no_bed"));
            return;
        }

        BlockPos hitPos = hitResult.getBlockPos();
        IBlockState state = player.world.getBlockState(hitPos);

        if (!(state.getBlock() instanceof BlockBed)) {
            sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.bedcheck.no_bed"));
            return;
        }

        BlockPos bedPos = hitPos;

        if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) bedPos = hitPos.offset(state.getValue(BlockHorizontal.FACING));

        WorldServer world = (WorldServer) player.world;
        int count = BedOccupancyAPI.getOccupantCount(world, bedPos);

        if (count == 0) {
            sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.bedcheck.empty"));
            return;
        }

        List<String> types = new ArrayList<>();
        int players = BedOccupancyAPI.getPlayerCount(world, bedPos);
        int others = count - players;

        if (players > 0) types.add(players + " player" + (players > 1 ? "s" : ""));
        if (others > 0) types.add(others + " other" + (others > 1 ? "s" : ""));

        String typeSummary = String.join(", ", types);
        sender.sendMessage(new TextComponentTranslation("commands.roomfortwo.bedcheck.occupants", count, typeSummary));
    }
}
