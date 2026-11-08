package sircow.roomfortwo.event;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mod.EventBusSubscriber(modid = "roomfortwo", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientViewportHandler {
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();

        if (!(camera.entity() instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction direction = livingEntity.getBedOrientation();
        if (direction == null) return;

        float baseYaw = direction.toYRot();
        int slot = BedOccupancyTracker.getSlot(livingEntity.getId());
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(livingEntity.getId());

        switch (sleepPos) {
            case NONE -> {
                if (slot % 2 == 0) {
                    event.setYaw(baseYaw - 90.0F);
                    event.setRoll(-90.0F);
                }
                else {
                    event.setYaw(baseYaw + 90.0F);
                    event.setRoll(90.0F);
                }
                event.setPitch(0.0F);
            }
            case LEFT -> {
                event.setYaw(baseYaw + 90.0F);
                event.setPitch(0.0F);
                event.setRoll(90.0F);
            }
            case RIGHT -> {
                event.setYaw(baseYaw - 90.0F);
                event.setPitch(0.0F);
                event.setRoll(-90.0F);
            }
            case FRONT -> {
                event.setYaw(baseYaw);
                event.setPitch(90.0F);
                event.setRoll(0.0F);
            }
            case BACK -> {
                event.setYaw(baseYaw);
                event.setPitch(-90.0F);
                event.setRoll(180.0F);
            }
        }
    }
}
