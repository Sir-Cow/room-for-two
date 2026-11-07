package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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
    @Shadow public abstract Vec3 position();
    @Shadow protected abstract void setPosition(Vec3 position);

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
        Quaternionf structurePose = new Quaternionf(rotation).mul(new Quaternionf().rotationYXZ((float) (Math.PI - Math.toRadians(baseYaw - 180.0F)), 0.0F, 0.0F).invert()
        );

        rotation.identity();
        rotation.rotateY((float) Math.toRadians(baseYaw));

        float moveForwards, moveUp;

        switch (sleepPos) {
            case NONE -> {
                if (slot % 2 == 0) rotation.rotateZ((float) Math.toRadians(-90.0));
                else rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                moveForwards = -0.75F;
                moveUp = 0.1F;
            }
            case LEFT -> {
                rotation.rotateZ((float) Math.toRadians(90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                moveForwards = -0.75F;
                moveUp = 0.1F;
            }
            case RIGHT -> {
                rotation.rotateZ((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));
                moveForwards = -0.75F;
                moveUp = 0.1F;
            }
            case FRONT -> {
                rotation.rotateX((float) Math.toRadians(-90.0));
                if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateZ((float) Math.toRadians(180.0));
                moveForwards = -0.25F;
                moveUp = 0.0F;
            }
            case BACK -> {
                rotation.rotateX((float) Math.toRadians(90.0));
                if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateZ((float) Math.toRadians(180.0));
                moveForwards = -0.5F;
                moveUp = 0.0F;
            }
            default -> {
                moveForwards = 0.0F;
                moveUp = 0.0F;
            }
        }

        rotation.premul(structurePose);
        move(moveForwards, moveUp, 0.0F);

        int level = slot / 2;

        if (level > 0) {
            Vec3 pos = this.position();
            this.setPosition(new Vec3(pos.x, pos.y + level * 0.4F, pos.z));
        }
    }
}
