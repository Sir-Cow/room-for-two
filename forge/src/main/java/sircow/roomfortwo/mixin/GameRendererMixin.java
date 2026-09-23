package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", shift = At.Shift.AFTER))
    private void roomfortwo$reapplySleepCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        roomfortwo$applySleepRotation();
    }

    @Inject(method = "extractCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", shift = At.Shift.AFTER))
    private void roomfortwo$reapplySleepCameraExtract(DeltaTracker deltaTracker, float worldPartialTicks, CallbackInfo ci) {
        roomfortwo$applySleepRotation();
    }

    @Unique
    private void roomfortwo$applySleepRotation() {
        CameraAccessor accessor = (CameraAccessor) mainCamera;
        Entity entity = accessor.roomfortwo$getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (!living.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction dir = living.getBedOrientation();
        if (dir == null) return;

        float baseYaw = dir.toYRot();
        int slot = BedOccupancyTracker.getSlot(entity.getId());
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(entity.getId());

        Quaternionf rotation = accessor.roomfortwo$getRotation();
        rotation.identity();
        rotation.rotateY((float) Math.toRadians(baseYaw));

        switch (sleepPos) {
            case NONE -> {
                if (slot % 2 == 0) rotation.rotateZ((float) Math.toRadians(-90.0));
                else rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
            }
            case LEFT -> {
                rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
            }
            case RIGHT -> {
                rotation.rotateZ((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
            }
            case FRONT -> {
                rotation.rotateX((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateZ((float) Math.toRadians(180.0));
            }
            case BACK -> {
                rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateZ((float) Math.toRadians(180.0));
            }
        }
    }
}
