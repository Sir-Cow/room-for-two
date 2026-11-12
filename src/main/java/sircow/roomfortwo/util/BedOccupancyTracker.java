package sircow.roomfortwo.util;

import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import sircow.roomfortwo.network.BedOccupancySyncPayload;
import sircow.roomfortwo.network.RoomForTwoNetwork;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BedOccupancyTracker {
    private static final Map<Integer, Integer> clientSlotCache = new ConcurrentHashMap<>();
    private static final Map<Integer, SleepPosition> clientSleepPosCache = new ConcurrentHashMap<>();
    private static final Map<BlockPos, List<Integer>> serverBedOrders = new ConcurrentHashMap<>();
    private static final Map<Integer, SleepPosition> serverSleepPosCache = new ConcurrentHashMap<>();

    private static int currentlyLeavingEntityId = -1;

    private BedOccupancyTracker() {}

    public static void setCurrentlyLeaving(int entityId) {
        currentlyLeavingEntityId = entityId;
    }

    public static void clearCurrentlyLeaving() {
        currentlyLeavingEntityId = -1;
    }

    public static int getSlot(int entityId) {
        Integer slot = clientSlotCache.get(entityId);
        return slot == null ? 0 : slot;
    }

    public static SleepPosition getSleepPos(int entityId) {
        SleepPosition position = clientSleepPosCache.get(entityId);
        return position == null ? SleepPosition.NONE : position;
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
        SleepPosition position = serverSleepPosCache.get(entityId);
        return position == null ? SleepPosition.NONE : position;
    }

    public static void removeServerSleepPos(int entityId) {
        serverSleepPosCache.remove(entityId);
    }

    public static Vec3d bedWorldCentre(BlockPos bedPos) {
        return new Vec3d(bedPos.getX() + 0.5D, bedPos.getY() + 0.5D, bedPos.getZ() + 0.5D);
    }

    public static AxisAlignedBB bedBox(BlockPos bedPos) {
        Vec3d centre = bedWorldCentre(bedPos);

        return new AxisAlignedBB(
                centre.x - 0.5D, centre.y - 0.5D, centre.z - 0.5D,
                centre.x + 0.5D, centre.y + 0.5D, centre.z + 0.5D
        ).grow(4.0D);
    }

    public static int getOccupantCount(WorldServer world, BlockPos bedPos) {
        int count = 0;

        for (EntityLivingBase occupant : getOccupants(world, bedPos)) {
            if (occupant.getEntityId() != currentlyLeavingEntityId) count++;
        }
        return count;
    }

    public static List<EntityLivingBase> getOccupants(WorldServer world, BlockPos bedPos) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);

        if (bedOrder != null) {
            List<EntityLivingBase> occupants = new ArrayList<>();

            for (int id : bedOrder) {
                if (id == -1) continue;
                Entity entity = world.getEntityByID(id);
                if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) occupants.add((EntityLivingBase) entity);
            }
            if (!occupants.isEmpty()) return occupants;
        }

        return world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                bedBox(bedPos),
                entity -> entity.isPlayerSleeping()
                        && entity instanceof EntityPlayer
                        && bedPos.equals(((EntityPlayer) entity).bedLocation)
        );
    }

    public static boolean hasOccupantType(WorldServer world, BlockPos bedPos, Class<? extends EntityLivingBase> type) {
        for (EntityLivingBase occupant : getOccupants(world, bedPos)) {
            if (type.isInstance(occupant)) return true;
        }
        return false;
    }

    public static int countOccupantType(WorldServer world, BlockPos bedPos, Class<? extends EntityLivingBase> type) {
        int count = 0;

        for (EntityLivingBase occupant : getOccupants(world, bedPos)) {
            if (type.isInstance(occupant)) count++;
        }
        return count;
    }

    public static void updateBedOccupancy(WorldServer world, BlockPos bedPos, int leavingEntityId, int enteringEntityId, Vec3d enteringPos) {
        if (bedPos == null) return;

        List<EntityPlayer> sleepers = world.getEntitiesWithinAABB(
                EntityPlayer.class,
                bedBox(bedPos),
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

            isEnteringLeftSide = (facing == EnumFacing.NORTH && enteringPos.x < middleX)
                    || (facing == EnumFacing.SOUTH && enteringPos.x > middleX)
                    || (facing == EnumFacing.WEST && enteringPos.z > middleZ)
                    || (facing == EnumFacing.EAST && enteringPos.z < middleZ);
        }

        Set<Integer> currentSleeperIds = new HashSet<>();
        for (EntityPlayer sleeper : sleepers) {
            currentSleeperIds.add(sleeper.getEntityId());
        }

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

        syncBed(world, bedPos, bedOrder);
    }

    // these methods are for making positions of occupants synced for client & server
    public static int getServerSlot(World world, BlockPos bedPos, int entityId) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);
        if (bedOrder == null) return -1;

        if (compact(bedOrder) && world instanceof WorldServer) {
            syncBed((WorldServer) world, bedPos, bedOrder);
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

    public static void restoreOccupant(WorldServer world, BlockPos bedPos, int entityId, int slot) {
        if (slot < 0) return;

        List<Integer> bedOrder = serverBedOrders.computeIfAbsent(bedPos, k -> new ArrayList<>());

        int target = slot;
        while (true) {
            while (bedOrder.size() <= target) bedOrder.add(-1);

            int existing = bedOrder.get(target);

            if (existing == -1 || existing == entityId || !isOccupantAt(world, bedPos, existing)) break;
            target += 2;
        }

        bedOrder.set(target, entityId);
        compact(bedOrder);
        syncBed(world, bedPos, bedOrder);
    }

    private static boolean isOccupantAt(WorldServer world, BlockPos bedPos, int entityId) {
        Entity entity = world.getEntityByID(entityId);
        return entity instanceof EntityLivingBase
                && ((EntityLivingBase) entity).isPlayerSleeping()
                && entity instanceof EntityPlayer
                && bedPos.equals(((EntityPlayer) entity).bedLocation);
    }

    public static void syncBed(WorldServer world, BlockPos bedPos) {
        List<Integer> bedOrder = serverBedOrders.get(bedPos);
        if (bedOrder == null) return;
        syncBed(world, bedPos, bedOrder);
    }

    public static void syncToPlayer(EntityPlayerMP player) {
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

        if (!syncMap.isEmpty()) RoomForTwoNetwork.sendToPlayer(new BedOccupancySyncPayload(syncMap, sleepPosMap), player);
    }

    private static void syncBed(WorldServer world, BlockPos bedPos, List<Integer> bedOrder) {
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

        RoomForTwoNetwork.sendToAllTracking(new BedOccupancySyncPayload(syncMap, sleepPosMap), bedPos, world.provider.getDimension());
    }
}
