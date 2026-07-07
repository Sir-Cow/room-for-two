package sircow.roomfortwo.util;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import sircow.roomfortwo.network.BedOccupancySyncPayload;
import sircow.roomfortwo.network.RoomForTwoNetwork;

import java.util.*;

public final class BedOccupancyTracker {
    private static final Map<Integer, Integer> clientSlotCache = new HashMap<>();
    private static final Map<BlockPos, List<Integer>> serverBedOrders = new HashMap<>();

    private BedOccupancyTracker() {}

    public static int getSlot(int entityId) {
        Integer slot = clientSlotCache.get(entityId);
        return slot == null ? 0 : slot;
    }

    public static void updateClientCache(Map<Integer, Integer> map) {
        clientSlotCache.putAll(map);
    }

    public static void cleanClientEntity(int entityId) {
        clientSlotCache.remove(entityId);
    }

    public static void updateBedOccupancy(WorldServer world, BlockPos bedPos, int leavingEntityId, int enteringEntityId, Vec3d enteringPos) {
        if (bedPos == null) return;

        List<EntityPlayer> sleepers = world.getEntitiesWithinAABB(
                EntityPlayer.class,
                new AxisAlignedBB(bedPos).grow(4.0D),
                player -> player.isPlayerSleeping()
                        && player.getEntityId() != leavingEntityId
                        && bedPos.equals(player.bedLocation)
        );

        boolean isEnteringLeftSide = false;

        if (enteringPos != null) {
            IBlockState state = world.getBlockState(bedPos);
            EnumFacing facing = state.getValue(BlockBed.FACING);

            double middleX = bedPos.getX() + 0.5D;
            double middleZ = bedPos.getZ() + 0.5D;

            isEnteringLeftSide = (facing == EnumFacing.NORTH && enteringPos.x > middleX)
                    || (facing == EnumFacing.SOUTH && enteringPos.x < middleX)
                    || (facing == EnumFacing.WEST && enteringPos.z < middleZ)
                    || (facing == EnumFacing.EAST && enteringPos.z > middleZ);
        }

        Set<Integer> currentSleeperIds = new HashSet<>();
        for (EntityLivingBase sleeper : sleepers) {
            currentSleeperIds.add(sleeper.getEntityId());
        }

        List<Integer> bedOrder = serverBedOrders.computeIfAbsent(bedPos, k -> new ArrayList<>());

        for (int i = 0; i < bedOrder.size(); i++) {
            int id = bedOrder.get(i);
            if (id == leavingEntityId || !currentSleeperIds.contains(id)) {
                bedOrder.set(i, -1);
            }
        }

        // if a player left who was previously in a slot BELOW a different player,
        // shift the entire slots for that side down to avoid someone flying in the air
        for (int i = 0; i < bedOrder.size(); i++) {
            if (bedOrder.get(i) != -1) continue;

            for (int x = i + 2; x < bedOrder.size(); x += 2) {
                bedOrder.set(x - 2, bedOrder.get(x));
                bedOrder.set(x, -1);
            }
        }

        // clean up empty back slots
        for (int i = bedOrder.size() - 1; i >= 0; i--) {
            if (bedOrder.get(i) == -1) {
                bedOrder.remove(i);
            }
            else break;
        }

        if (enteringPos != null) {
            boolean addedInBetween = false;

            for (int i = 0; i < bedOrder.size(); i++) {
                final int elem = bedOrder.get(i);
                if (elem == -1) {
                    if ((!isEnteringLeftSide && i % 2 == 0) || (isEnteringLeftSide && i % 2 == 1)) {
                        bedOrder.set(i, enteringEntityId);
                        addedInBetween = true;
                        break;
                    }
                }
            }

            if (!addedInBetween) {
                if (bedOrder.size() % 2 != (isEnteringLeftSide ? 1 : 0)) {
                    bedOrder.add(-1);
                }
                bedOrder.add(enteringEntityId);
            }
        }

        if (bedOrder.isEmpty()) {
            serverBedOrders.remove(bedPos);
            return;
        }

        Map<Integer, Integer> syncMap = new HashMap<>();
        for (int i = 0; i < bedOrder.size(); i++) {
            syncMap.put(bedOrder.get(i), i);
        }

        RoomForTwoNetwork.sendToAllTracking(new BedOccupancySyncPayload(syncMap), bedPos, world.provider.getDimension());
    }
}
