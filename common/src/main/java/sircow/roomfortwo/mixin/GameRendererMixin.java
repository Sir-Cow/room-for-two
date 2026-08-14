package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.platform.Services;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.Objects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique private static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    @Unique private static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    @Unique private static final Vector3f Z_AXIS = new Vector3f(0.0F, 0.0F, 1.0F);

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lcom/mojang/math/Matrix4f;)V", shift = At.Shift.BEFORE))
    private void roomfortwo$adjustSleepCamera(float partialTick, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        LivingEntity livingEntity = minecraft.player;
        if (!livingEntity.isSleeping()) return;
        if (!minecraft.options.getCameraType().isFirstPerson()) return;

        Direction dir = livingEntity.getBedOrientation();
        if (dir == null) return;

        int slot = BedOccupancyTracker.getSlot(livingEntity.getId());

        poseStack.mulPose(Y_AXIS.rotationDegrees(dir.toYRot() - 180.0F));

        float xRotateL, yRotateL, zRotateL, xRotateR, yRotateR, zRotateR;
        switch (dir) {
            case NORTH -> {
                xRotateR = 90.0F;
                yRotateR = 0.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case SOUTH -> {
                xRotateR = -90.0F;
                yRotateR = 0.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case WEST -> {
                xRotateR = 0.0F;
                yRotateR = 180.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR + 180.0F;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case EAST -> {
                xRotateR = 0.0F;
                yRotateR = 0.0F;
                zRotateR = 90.0F;
                xRotateL = xRotateR + 180.0F;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            default -> {
                xRotateL = xRotateR = 0.0F;
                yRotateL = yRotateR = 0.0F;
                zRotateL = zRotateR = 0.0F;
            }
        }

        boolean side = slot % 2 == 0;

        if (side) {
            poseStack.mulPose(X_AXIS.rotationDegrees(xRotateR));
            poseStack.mulPose(Y_AXIS.rotationDegrees(yRotateR));
            poseStack.mulPose(Z_AXIS.rotationDegrees(zRotateR));
        }
        else {
            poseStack.mulPose(X_AXIS.rotationDegrees(xRotateL));
            poseStack.mulPose(Y_AXIS.rotationDegrees(yRotateL));
            poseStack.mulPose(Z_AXIS.rotationDegrees(zRotateL));
        }

        if (side) {
            switch (dir) {
                case NORTH -> poseStack.translate(-0.5D, -0.1D, 0.0D);
                case SOUTH -> poseStack.translate(0.5D, -0.1D, 0.0D);
                case WEST -> poseStack.translate(0.0D, -0.1D, 0.5D);
                case EAST -> poseStack.translate(0.0D, -0.1D, -0.5D);
            }
        }
        else {
            switch (dir) {
                case NORTH -> poseStack.translate(0.75D, -0.1D, 0.0D);
                case SOUTH -> poseStack.translate(-0.75D, -0.1D, 0.0D);
                case WEST -> poseStack.translate(0.0D, -0.1D, -0.75D);
                case EAST -> poseStack.translate(0.0D, -0.1D, 0.75D);
            }
        }

        if (Objects.equals(Services.PLATFORM.getPlatformName(), "Forge")) {
            poseStack.translate(-0.1D, 0.0D, 0.0D);
        }
    }

    @Unique
    private static float wrapYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw > 180.0F) yaw -= 360.0F;
        if (yaw <= -180.0F) yaw += 360.0F;
        return yaw;
    }
}