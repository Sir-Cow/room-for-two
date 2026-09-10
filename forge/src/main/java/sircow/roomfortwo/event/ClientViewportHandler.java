package sircow.roomfortwo.event;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = "roomfortwo", value = Dist.CLIENT)
public class ClientViewportHandler {
    private static final Method MOVE_CAMERA_METHOD = ObfuscationReflectionHelper.findMethod(
            Camera.class, "m_90568_", double.class, double.class, double.class
    );

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();

        if (!(camera.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction direction = livingEntity.getBedOrientation();
        if (direction == null) return;

        float baseYaw = direction.toYRot() - 180.0F;
        float xRotateL, yRotateL, zRotateL, xRotateR, yRotateR, zRotateR, targetPitch, targetYaw, targetRoll;

        xRotateR = 0.0F;
        yRotateR = 90.0F;
        zRotateR = 90.0F;
        xRotateL = -xRotateR;
        yRotateL = -wrapYaw(yRotateR);
        zRotateL = -zRotateR;

        int slot = BedOccupancyTracker.getSlot(livingEntity.getId());

        if (slot % 2 == 0) {
            targetPitch = xRotateR;
            targetYaw = baseYaw + yRotateR;
            targetRoll = zRotateR;
            moveCameraFree(camera, 0.0, -0.1, 0.5);
        }
        else {
            targetPitch = xRotateL;
            targetYaw = baseYaw + yRotateL;
            targetRoll = zRotateL;
            moveCameraFree(camera, 0.0, -0.1, -0.5);
        }
        event.setPitch(targetPitch);
        event.setYaw(targetYaw);
        event.setRoll(targetRoll);
    }

    private static float wrapYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw > 180.0F) yaw -= 360.0F;
        if (yaw <= -180.0F) yaw += 360.0F;
        return yaw;
    }

    private static void moveCameraFree(Camera camera, double x, double y, double z) {
        try {
            MOVE_CAMERA_METHOD.invoke(camera, x, y, z);
        }
        catch (Exception ignored) {}
    }
}
