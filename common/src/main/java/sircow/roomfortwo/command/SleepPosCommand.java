package sircow.roomfortwo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

public class SleepPosCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sleeppos")
                .then(Commands.argument("position", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                new String[]{"left", "right", "front", "back", "none"}, builder))
                        .executes(context -> {
                            String posName = StringArgumentType.getString(context, "position");
                            SleepPosition position = SleepPosition.fromString(posName);

                            if (position == null) {
                                context.getSource().sendFailure(Component.translatable("commands.roomfortwo.sleeppos.invalid", posName));
                                return 0;
                            }

                            ServerPlayer player = context.getSource().getPlayerOrException();

                            BedOccupancyTracker.setServerSleepPos(player.getId(), position);
                            context.getSource().sendSuccess(() -> Component.translatable("commands.roomfortwo.sleeppos.set", position.getName()), false);
                            return 1;
                        }))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    SleepPosition current = BedOccupancyTracker.getServerSleepPos(player.getId());

                    context.getSource().sendSuccess(() -> Component.translatable("commands.roomfortwo.sleeppos.query", current.getName()), false);
                    return 1;
                })
        );
    }
}
