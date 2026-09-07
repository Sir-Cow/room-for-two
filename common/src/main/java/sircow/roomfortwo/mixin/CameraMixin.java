package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Quaternionf rotation;
    @Shadow private Entity entity;

    @Shadow protected abstract void move(float forwards, float up, float right);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void roomfortwo$adjustSleepCamera(float partialTicks, CallbackInfo ci) {
        if (!(this.entity instanceof LivingEntity living)) return;
        if (!living.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction dir = living.getBedOrientation();
        if (dir == null) return;

        float baseYaw = dir.toYRot();
        int slot = BedOccupancyTracker.getSlot(this.entity.getId());
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(this.entity.getId());
        float verticalOffset = ((float) slot / 2) * 0.25F;

        rotation.identity();
        rotation.rotateY((float) Math.toRadians(baseYaw));

        switch (sleepPos) {
            case NONE -> {
                if (slot % 2 == 0) rotation.rotateZ((float) Math.toRadians(-90.0));
                else rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                move(-0.75F, 0.1F, verticalOffset);
            }
            case LEFT -> {
                rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                move(-0.75F, 0.1F, verticalOffset);
            }
            case RIGHT -> {
                rotation.rotateZ((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                move(-0.75F, 0.1F, verticalOffset);
            }
            case FRONT -> {
                rotation.rotateX((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateZ((float) Math.toRadians(180.0));
                move(-0.25F, 0.0F, verticalOffset);
            }
            case BACK -> {
                rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateZ((float) Math.toRadians(180.0));
                move(-0.5F, 0.0F, verticalOffset);
            }
        }
    }
}
