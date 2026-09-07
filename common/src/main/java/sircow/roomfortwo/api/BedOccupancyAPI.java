package sircow.roomfortwo.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.List;

/**
 * API for getting bed occupancy info
 * All methods use {@link ServerLevel} and a bed {@link BlockPos}.
 */
public final class BedOccupancyAPI {
    private BedOccupancyAPI() {}

    /**
     * Returns the number of entities currently sleeping in the bed at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @return number of sleeping occupants
     */
    public static int getOccupantCount(ServerLevel serverLevel, BlockPos bedPos) {
        return BedOccupancyTracker.getOccupantCount(serverLevel, bedPos);
    }

    /**
     * Returns a list of all entities currently sleeping in the bed at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @return list of sleeping occupants
     */
    public static List<LivingEntity> getOccupants(ServerLevel serverLevel, BlockPos bedPos) {
        return BedOccupancyTracker.getOccupants(serverLevel, bedPos);
    }

    /**
     * Checks if at least one {@link Player} is sleeping in the bed at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @return {@code true} if a player is sleeping in the bed
     */
    public static boolean hasPlayer(ServerLevel serverLevel, BlockPos bedPos) {
        return BedOccupancyTracker.hasOccupantType(serverLevel, bedPos, Player.class);
    }

    /**
     * Returns the number of {@link Player}s currently sleeping in the bed at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @return number of players sleeping in the bed
     */
    public static int getPlayerCount(ServerLevel serverLevel, BlockPos bedPos) {
        return BedOccupancyTracker.countOccupantType(serverLevel, bedPos, Player.class);
    }

    /**
     * Checks whether at least one entity of the given type is sleeping in the bed at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @param type entity class to check for (e.g. {@code Player.class}, {@code Villager.class})
     * @return {@code true} if an entity of the given type is sleeping in the bed
     */
    public static boolean hasOccupantType(ServerLevel serverLevel, BlockPos bedPos, Class<? extends LivingEntity> type) {
        return BedOccupancyTracker.hasOccupantType(serverLevel, bedPos, type);
    }

    /**
     * Returns the number of entities sleeping in the bed of the given type at the given position
     *
     * @param serverLevel server level containing the bed
     * @param bedPos position of the bed block
     * @param type entity class to count (e.g. {@code Player.class}, {@code Villager.class})
     * @return number of entities sleeping in the bed of the given type
     */
    public static int countOccupantType(ServerLevel serverLevel, BlockPos bedPos, Class<? extends LivingEntity> type) {
        return BedOccupancyTracker.countOccupantType(serverLevel, bedPos, type);
    }
}
