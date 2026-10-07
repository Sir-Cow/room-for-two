package sircow.roomfortwo.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.List;

/**
 * API for getting bed occupancy info
 * All methods use {@link WorldServer} and a bed {@link BlockPos}.
 */
public final class BedOccupancyAPI {
    private BedOccupancyAPI() {}

    /**
     * Returns the number of entities currently sleeping in the bed at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @return number of sleeping occupants
     */
    public static int getOccupantCount(WorldServer world, BlockPos bedPos) {
        return BedOccupancyTracker.getOccupantCount(world, bedPos);
    }

    /**
     * Returns a list of all entities currently sleeping in the bed at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @return list of sleeping occupants
     */
    public static List<EntityLivingBase> getOccupants(WorldServer world, BlockPos bedPos) {
        return BedOccupancyTracker.getOccupants(world, bedPos);
    }

    /**
     * Checks if at least one {@link EntityPlayer} is sleeping in the bed at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @return {@code true} if a player is sleeping in the bed
     */
    public static boolean hasPlayer(WorldServer world, BlockPos bedPos) {
        return BedOccupancyTracker.hasOccupantType(world, bedPos, EntityPlayer.class);
    }

    /**
     * Returns the number of {@link EntityPlayer}s currently sleeping in the bed at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @return number of players sleeping in the bed
     */
    public static int getPlayerCount(WorldServer world, BlockPos bedPos) {
        return BedOccupancyTracker.countOccupantType(world, bedPos, EntityPlayer.class);
    }

    /**
     * Checks whether at least one entity of the given type is sleeping in the bed at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @param type entity class to check for (e.g. {@code EntityPlayer.class})
     * @return {@code true} if an entity of the given type is sleeping in the bed
     */
    public static boolean hasOccupantType(WorldServer world, BlockPos bedPos, Class<? extends EntityLivingBase> type) {
        return BedOccupancyTracker.hasOccupantType(world, bedPos, type);
    }

    /**
     * Returns the number of entities sleeping in the bed of the given type at the given position
     *
     * @param world server world containing the bed
     * @param bedPos position of the bed block
     * @param type entity class to count (e.g. {@code EntityPlayer.class})
     * @return number of entities of the given type sleeping in the bed
     */
    public static int countOccupantType(WorldServer world, BlockPos bedPos, Class<? extends EntityLivingBase> type) {
        return BedOccupancyTracker.countOccupantType(world, bedPos, type);
    }
}
