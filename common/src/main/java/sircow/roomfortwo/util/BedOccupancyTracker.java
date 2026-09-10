package sircow.roomfortwo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import sircow.roomfortwo.platform.Services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class BedOccupancyTracker {
    private static final Map<Integer, Integer> clientSlotCache = new ConcurrentHashMap<>();
    private static final Map<Integer, SleepPosition> clientSleepPosCache = new ConcurrentHashMap<>();
    private static final Map<BlockPos, List<Integer>> serverBedOrders = new ConcurrentHashMap<>();
    private static final Map<Integer, SleepPosition> serverSleepPosCache = new ConcurrentHashMap<>();

    private BedOccupancyTracker() {}

    public static int getSlot(int entityId) {
        return clientSlotCache.getOrDefault(entityId, 0);
    }

    public static SleepPosition getSleepPos(int entityId) {
        return clientSleepPosCache.getOrDefault(entityId, SleepPosition.NONE);
    }

    public static void updateClientCache(Map<Integer, Integer> map, Map<Integer, SleepPosition> sleepPositions) {
        clientSlotCache.putAll(map);
        clientSleepPosCache.putAll(sleepPositions);
    }

    public static void cleanClientEntity(int entityId) {
        clientSlotCache.remove(entityId);
        clientSleepPosCache.remove(entityId);
    }

    public static void setServerSleepPos(int entityId, SleepPosition sleepPos) {
        serverSleepPosCache.put(entityId, sleepPos);
    }

    public static SleepPosition getServerSleepPos(int entityId) {
        return serverSleepPosCache.getOrDefault(entityId, SleepPosition.NONE);
    }

    public static void removeServerSleepPos(int entityId) {
        serverSleepPosCache.remove(entityId);
    }

    public static int getOccupantCount(ServerLevel level, BlockPos bedPos) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);
        if (bedOrder != null) {
            int count = 0;
            for (int id : bedOrder) {
                if (id == -1) continue;
                if (level.getEntity(id) instanceof LivingEntity e && e.isSleeping()) {
                    count++;
                }
            }
            if (count > 0) return count;
        }
        return level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(bedPos).inflate(4.0), entity -> entity.isSleeping() && entity.getSleepingPos().map(bedPos::equals).orElse(false)
        ).size();
    }

    public static List<LivingEntity> getOccupants(ServerLevel level, BlockPos bedPos) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);

        if (bedOrder != null) {
            List<LivingEntity> occupants = new ArrayList<>();

            for (int id : bedOrder) {
                if (id == -1) continue;
                if (level.getEntity(id) instanceof LivingEntity e && e.isSleeping()) occupants.add(e);
            }
            if (!occupants.isEmpty()) return occupants;
        }
        return level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(bedPos).inflate(4.0), entity -> entity.isSleeping() && entity.getSleepingPos().map(bedPos::equals).orElse(false)
        );
    }

    public static boolean hasOccupantType(ServerLevel level, BlockPos bedPos, Class<? extends LivingEntity> type) {
        return getOccupants(level, bedPos).stream().anyMatch(type::isInstance);
    }

    public static int countOccupantType(ServerLevel level, BlockPos bedPos, Class<? extends LivingEntity> type) {
        return (int) getOccupants(level, bedPos).stream().filter(type::isInstance).count();
    }

    public static void updateBedOccupancy(ServerLevel level, BlockPos bedPos, int leavingEntityId, int enteringEntityId, Vec3 enteringPos) {
        if (bedPos == null) return;

        List<LivingEntity> sleepers = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(bedPos).inflate(4.0),
                entity -> entity.isSleeping()
                        && entity.getId() != leavingEntityId
                        && entity.getSleepingPos().map(bedPos::equals).orElse(false)
        );

        boolean isEnteringLeftSide = false;

        if (enteringPos != null) {
            BlockState bedState = level.getBlockState(bedPos);
            Direction facing = bedState.getValue(BedBlock.FACING);

            final float middleX = bedPos.getX() + 0.5F;
            final float middleZ = bedPos.getZ() + 0.5F;
            final float playerX = (float) enteringPos.get(Direction.Axis.X);
            final float playerZ = (float) enteringPos.get(Direction.Axis.Z);

            isEnteringLeftSide = (facing == Direction.NORTH && playerX < middleX)
                    || (facing == Direction.SOUTH && playerX > middleX)
                    || (facing == Direction.WEST && playerZ > middleZ)
                    || (facing == Direction.EAST && playerZ < middleZ);
        }

        Set<Integer> currentSleeperIds = sleepers.stream().map(LivingEntity::getId).collect(Collectors.toSet());
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
        for (int i = bedOrder.size() - 1; i > -1; i--) {
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

        Map<Integer, SleepPosition> sleepPosMap = new HashMap<>();

        for (int id : syncMap.keySet()) {
            SleepPosition sleepPos = serverSleepPosCache.get(id);
            if (sleepPos != null) sleepPosMap.put(id, sleepPos);
        }

        Services.NETWORK.broadcastBedOccupancy(level, bedPos, syncMap, sleepPosMap);
    }
}
