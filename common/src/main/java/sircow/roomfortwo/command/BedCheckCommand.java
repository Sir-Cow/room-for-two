package sircow.roomfortwo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import sircow.roomfortwo.api.BedOccupancyAPI;

import java.util.ArrayList;
import java.util.List;

public class BedCheckCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bedcheck")
            .requires(s -> s.hasPermission(2))
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                BlockHitResult hitResult = getPlayerLookTarget(player);

                if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
                    context.getSource().sendFailure(Component.translatable("commands.roomfortwo.bedcheck.no_bed"));
                    return 0;
                }

                BlockPos hitPos = hitResult.getBlockPos();
                BlockState state = player.level().getBlockState(hitPos);

                if (!(state.getBlock() instanceof BedBlock)) {
                    context.getSource().sendFailure(Component.translatable("commands.roomfortwo.bedcheck.no_bed"));
                    return 0;
                }

                BlockPos bedPos = hitPos;

                if (state.getValue(BedBlock.PART) != BedPart.HEAD) bedPos = hitPos.relative(state.getValue(BedBlock.FACING));

                ServerLevel level = (ServerLevel) player.level();
                int count = BedOccupancyAPI.getOccupantCount(level, bedPos);

                if (count == 0) {
                    context.getSource().sendSuccess(() -> Component.translatable("commands.roomfortwo.bedcheck.empty"), false);
                    return 1;
                }

                List<String> types = new ArrayList<>();
                int players = BedOccupancyAPI.getPlayerCount(level, bedPos);
                int others = count - players;

                if (players > 0) types.add(players + " player" + (players > 1 ? "s" : ""));
                if (others > 0) types.add(others + " other" + (others > 1 ? "s" : ""));

                String typeSummary = String.join(", ", types);
                context.getSource().sendSuccess(() -> Component.translatable("commands.roomfortwo.bedcheck.occupants", count, typeSummary), false);
                return count;
            })
        );
    }

    private static BlockHitResult getPlayerLookTarget(ServerPlayer player) {
        double reach = 5.0;
        HitResult result = player.pick(reach, 0.0F, false);

        if (result instanceof BlockHitResult blockHit) return blockHit;
        return null;
    }
}
