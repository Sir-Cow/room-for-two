package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.nio.FloatBuffer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lcom/mojang/math/Matrix4f;)V"))
    private void roomfortwo$adjustSleepCamera(float partialTick, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        LivingEntity livingEntity = minecraft.player;
        if (!livingEntity.isSleeping()) return;
        if (!minecraft.options.getCameraType().isFirstPerson()) return;

        Direction dir = livingEntity.getBedOrientation();
        if (dir == null) return;

        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(livingEntity.getId());

        poseStack.setIdentity();

        switch (sleepPos) {
            case NONE -> {
                int slot = BedOccupancyTracker.getSlot(livingEntity.getId());
                float yaw, roll;

                if (slot % 2 == 0) {
                    yaw = dir.toYRot() + 90;
                    roll = 90.0F;
                }
                else {
                    yaw = dir.toYRot() - 90;
                    roll = -90.0F;
                }
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
            }
            case LEFT -> {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(dir.toYRot() - 90));
            }
            case RIGHT -> {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(dir.toYRot() + 90));
            }
            case FRONT -> {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(dir.toYRot() + 180));
            }
            case BACK -> {
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(dir.toYRot() + 180));
            }
        }

        if (sleepPos == SleepPosition.NONE || sleepPos == SleepPosition.LEFT || sleepPos == SleepPosition.RIGHT) {
            Matrix3f rotation = new Matrix3f(poseStack.last().normal());
            FloatBuffer buffer = FloatBuffer.allocate(9);

            rotation.store(buffer);
            poseStack.translate(-buffer.get(2) * 0.5F, -buffer.get(5) * 0.5F, -buffer.get(8) * 0.5F);
        }

        Matrix3f inverseViewRotation = new Matrix3f(poseStack.last().normal());

        inverseViewRotation.invert();
        RenderSystem.setInverseViewRotationMatrix(inverseViewRotation);
    }
}
