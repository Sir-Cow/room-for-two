package sircow.roomfortwo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
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
    private static final Map<Integer, VillagerApproach> serverApproachCache = new ConcurrentHashMap<>();
    private static final double approachLateralDist = 0.75D;
    private static final double approachMaxDist = 2.5D;

    private record VillagerApproach(BlockPos bedPos, Vec3 pos) {}

    private static int currentlyLeavingEntityId = -1;

    private BedOccupancyTracker() {}

    public static void setCurrentlyLeaving(int entityId) {
        currentlyLeavingEntityId = entityId;
    }

    public static void clearCurrentlyLeaving() {
        currentlyLeavingEntityId = -1;
    }

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

    // methods that track where a villager enters a bed from to determine position
    public static void recordVillagerApproach(ServerLevel level, Villager villager) {
        int villagerId = villager.getId();
        Optional<GlobalPos> home = villager.getBrain().getMemory(MemoryModuleType.HOME);

        if (home.isEmpty() || !home.get().dimension().equals(level.dimension())) {
            serverApproachCache.remove(villagerId);
            return;
        }

        BlockPos bedPos = home.get().pos();
        Vec3 villagerPos = villager.position();
        Vec3 villagerInBedSpace = villagerPos;
        double offsetX = villagerInBedSpace.x - (bedPos.getX() + 0.5D);
        double offsetZ = villagerInBedSpace.z - (bedPos.getZ() + 0.5D);

        if (offsetX * offsetX + offsetZ * offsetZ > approachMaxDist * approachMaxDist) {
            serverApproachCache.remove(villagerId);
            return;
        }

        BlockState bedState = level.getBlockState(bedPos);
        if (!bedState.hasProperty(BedBlock.FACING)) {
            serverApproachCache.remove(villagerId);
            return;
        }

        Direction facing = bedState.getValue(BedBlock.FACING);
        double lateralDist = facing.getAxis() == Direction.Axis.X ? offsetZ : offsetX;

        if (Math.abs(lateralDist) >= approachLateralDist) {
            serverApproachCache.put(villagerId, new VillagerApproach(bedPos, villagerPos));
        }
    }

    public static Vec3 takeVillagerApproachPos(int entityId, BlockPos bedPos, Vec3 enteringPos) {
        VillagerApproach approach = serverApproachCache.remove(entityId);

        if (approach == null || !approach.bedPos().equals(bedPos)) return enteringPos;
        return approach.pos();
    }

    public static void removeVillagerApproach(int entityId) {
        serverApproachCache.remove(entityId);
    }

    public static Vec3 bedWorldCentre(Level level, BlockPos bedPos) {
        return new Vec3(bedPos.getX() + 0.5D, bedPos.getY() + 0.5D, bedPos.getZ() + 0.5D);
    }

    public static AABB bedBox(Level level, BlockPos bedPos) {
        Vec3 centre = bedWorldCentre(level, bedPos);

        return new AABB(
                centre.x - 0.5D, centre.y - 0.5D, centre.z - 0.5D,
                centre.x + 0.5D, centre.y + 0.5D, centre.z + 0.5D
        ).inflate(4.0D);
    }

    public static int getOccupantCount(ServerLevel level, BlockPos bedPos) {
        int count = 0;

        for (LivingEntity occupant : getOccupants(level, bedPos)) {
            if (occupant.getId() != currentlyLeavingEntityId) count++;
        }
        return count;
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
                bedBox(level, bedPos), entity -> entity.isSleeping() && entity.getSleepingPos().map(bedPos::equals).orElse(false)
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
                bedBox(level, bedPos),
                entity -> entity.isSleeping()
                        && entity.getId() != leavingEntityId
                        && entity.getSleepingPos().map(bedPos::equals).orElse(false)
        );

        boolean isEnteringLeftSide = false;

        if (enteringPos != null) {
            BlockState bedState = level.getBlockState(bedPos);
            if (!bedState.hasProperty(BedBlock.FACING)) return;

            Direction facing = bedState.getValue(BedBlock.FACING);
            Vec3 entry = enteringPos;
            final double middleX = bedPos.getX() + 0.5D;
            final double middleZ = bedPos.getZ() + 0.5D;
            final double playerX = entry.get(Direction.Axis.X);
            final double playerZ = entry.get(Direction.Axis.Z);

            isEnteringLeftSide = (facing == Direction.NORTH && playerX < middleX)
                    || (facing == Direction.SOUTH && playerX > middleX)
                    || (facing == Direction.WEST && playerZ > middleZ)
                    || (facing == Direction.EAST && playerZ < middleZ);
        }

        Set<Integer> currentSleeperIds = sleepers.stream().map(LivingEntity::getId).collect(Collectors.toSet());
        List<Integer> bedOrder = serverBedOrders.computeIfAbsent(bedPos, k -> new ArrayList<>());

        for (int i = 0; i < bedOrder.size(); i++) {
            int id = bedOrder.get(i);
            if (id == leavingEntityId || !currentSleeperIds.contains(id)) bedOrder.set(i, -1);
        }
        compact(bedOrder);

        if (enteringPos != null && !bedOrder.contains(enteringEntityId)) {
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

        syncBed(level, bedPos, bedOrder);
    }

    // these methods are for making positions of occupants synced for client & server
    public static int getServerSlot(Level level, BlockPos bedPos, int entityId) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);
        if (bedOrder == null) return -1;

        if (compact(bedOrder) && level instanceof ServerLevel serverLevel) {
            syncBed(serverLevel, bedPos, bedOrder);
        }
        return bedOrder.indexOf(entityId);
    }

    // reserialise order per side
    private static boolean compact(List<Integer> bedOrder) {
        List<Integer> evens = new ArrayList<>();
        List<Integer> odds = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        for (int i = 0; i < bedOrder.size(); i++) {
            int id = bedOrder.get(i);
            if (id == -1 || !seen.add(id)) continue;
            if (i % 2 == 0) evens.add(id);
            else odds.add(id);
        }

        int size = Math.max(evens.isEmpty() ? 0 : evens.size() * 2 - 1, odds.size() * 2);
        List<Integer> compacted = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                int evenIndex = i / 2;

                compacted.add(evenIndex < evens.size() ? evens.get(evenIndex) : -1);
            }
            else {
                int oddIndex = i / 2;

                compacted.add(oddIndex < odds.size() ? odds.get(oddIndex) : -1);
            }
        }

        if (compacted.equals(bedOrder)) return false;
        bedOrder.clear();
        bedOrder.addAll(compacted);
        return true;
    }

    public static void restoreOccupant(ServerLevel level, BlockPos bedPos, int entityId, int slot) {
        if (slot < 0) return;

        List<Integer> bedOrder = serverBedOrders.computeIfAbsent(bedPos, k -> new ArrayList<>());

        int target = slot;
        while (true) {
            while (bedOrder.size() <= target) bedOrder.add(-1);

            int existing = bedOrder.get(target);

            if (existing == -1 || existing == entityId || !isLivingEntityOccupant(level, bedPos, existing)) break;
            target += 2;
        }

        bedOrder.set(target, entityId);
        compact(bedOrder);
        syncBed(level, bedPos, bedOrder);
    }

    private static boolean isLivingEntityOccupant(ServerLevel level, BlockPos bedPos, int entityId) {
        return level.getEntity(entityId) instanceof LivingEntity occupant && occupant.isSleeping() && occupant.getSleepingPos().map(bedPos::equals).orElse(false);
    }

    public static void syncBed(ServerLevel level, BlockPos bedPos) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);
        if (bedOrder == null) return;
        syncBed(level, bedPos, bedOrder);
    }

    public static void syncToPlayer(ServerPlayer player) {
        Map<Integer, Integer> syncMap = new HashMap<>();
        Map<Integer, SleepPosition> sleepPosMap = new HashMap<>();

        for (List<Integer> bedOrder : serverBedOrders.values()) {
            for (int i = 0; i < bedOrder.size(); i++) {
                int id = bedOrder.get(i);
                if (id == -1) continue;

                syncMap.put(id, i);
                SleepPosition sleepPos = serverSleepPosCache.get(id);
                if (sleepPos != null) sleepPosMap.put(id, sleepPos);
            }
        }

        if (!syncMap.isEmpty()) Services.NETWORK.sendBedOccupancy(player, syncMap, sleepPosMap);
    }

    private static void syncBed(ServerLevel level, BlockPos bedPos, List<Integer> bedOrder) {
        Map<Integer, Integer> syncMap = new HashMap<>();

        for (int i = 0; i < bedOrder.size(); i++) {
            int id = bedOrder.get(i);
            if (id != -1) syncMap.put(id, i);
        }

        Map<Integer, SleepPosition> sleepPosMap = new HashMap<>();

        for (int id : syncMap.keySet()) {
            SleepPosition sleepPos = serverSleepPosCache.get(id);
            if (sleepPos != null) sleepPosMap.put(id, sleepPos);
        }

        Services.NETWORK.broadcastBedOccupancy(level, bedPos, syncMap, sleepPosMap);
    }
}
