package sircow.roomfortwo.event;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = "roomfortwo", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientViewportHandler {
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();

        if (!(camera.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction direction = livingEntity.getBedOrientation();
        if (direction == null) return;

        float baseYaw = direction.toYRot();
        int slot = BedOccupancyTracker.getSlot(livingEntity.getId());
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(livingEntity.getId());

        switch (sleepPos) {
            case NONE -> {
                float targetYaw, targetRoll;
                if (slot % 2 == 0) {
                    targetYaw = baseYaw - 90.0F;
                    targetRoll = -90.0F;
                }
                else {
                    targetYaw = baseYaw + 90.0F;
                    targetRoll = 90.0F;
                }
                event.setYaw(targetYaw);
                event.setPitch(0.0F);
                event.setRoll(targetRoll);
                moveCameraFree(camera, slot, -0.5F, 0.1F);
            }
            case LEFT -> {
                event.setYaw(baseYaw + 90.0F);
                event.setPitch(0.0F);
                event.setRoll(90.0F);
                moveCameraFree(camera, slot, -0.5F, 0.1F);
            }
            case RIGHT -> {
                event.setYaw(baseYaw - 90.0F);
                event.setPitch(0.0F);
                event.setRoll(-90.0F);
                moveCameraFree(camera, slot, -0.5F, 0.1F);
            }
            case FRONT -> {
                event.setYaw(baseYaw);
                event.setPitch(90.0F);
                event.setRoll(0.0F);
                if (direction == Direction.EAST || direction == Direction.WEST) {
                    event.setRoll(180.0F);
                }
                moveCameraFree(camera, slot, -0.5F, 0.0F);
            }
            case BACK -> {
                event.setYaw(baseYaw);
                event.setPitch(-90.0F);
                event.setRoll(0.0F);
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    event.setRoll(180.0F);
                }
                moveCameraFree(camera, slot, -0.25F, 0.0F);
            }
        }
    }

    private static void moveCameraFree(Camera camera, int slot, float forward, float up) {
        try {
            Method moveMethod = Camera.class.getDeclaredMethod("move", float.class, float.class, float.class);
            moveMethod.setAccessible(true);
            float verticalOffset = ((float) slot / 2) * 0.25F;
            moveMethod.invoke(camera, forward, up, verticalOffset);
        }
        catch (Exception ignored) {}
    }
}
