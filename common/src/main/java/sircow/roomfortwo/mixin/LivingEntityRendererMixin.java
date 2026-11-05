package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SableInterop;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Optional;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBedOrientation()Lnet/minecraft/core/Direction;", shift = At.Shift.AFTER))
    private void roomfortwo$cancelDuplicateSableRotation(LivingEntity livingEntity, float partialTick, float bodyYaw, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (livingEntity.getBedOrientation() == null) return;

        Optional<BlockPos> sleepingPos = livingEntity.getSleepingPos();
        if (sleepingPos.isEmpty()) return;

        Quaternionf structure = SableInterop.renderOrientation(livingEntity.level(), sleepingPos.get());
        if (structure == null) return;

        poseStack.mulPose(new Quaternionf(structure).invert());
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void roomfortwo$rotateSleepingEntities(LivingEntity livingEntity, PoseStack poseStack, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        if (!livingEntity.hasPose(Pose.SLEEPING)) return;
        if (!livingEntity.isSleeping()) return;

        int id = livingEntity.getId();
        int slot = BedOccupancyTracker.getSlot(id);
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(id);
        float baseZ = -0.15F;

        float zOffset = -(float) (slot / 2) * 0.4F;
        float finalZ = baseZ + zOffset;
        Minecraft minecraft = Minecraft.getInstance();

        boolean firstPerson = minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
        boolean localPlayer = livingEntity == minecraft.player;

        switch (sleepPos) {
            case NONE -> {
                if (slot % 2 == 0) poseStack.translate(-0.25F, 0.0F, finalZ);
                else poseStack.translate(0.25F, 0.0F, finalZ);
                if (firstPerson && localPlayer) {
                    if (slot % 2 == 0) poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    else poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                }
                else {
                    if (slot % 2 == 0) poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                    else poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                }
            }
            case LEFT -> {
                poseStack.translate(0.25F, 0.0F, finalZ);
                if (firstPerson && localPlayer) poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                else poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case RIGHT -> {
                poseStack.translate(-0.25F, 0.0F, finalZ);
                if (firstPerson && localPlayer) poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                else poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            case FRONT -> {
                poseStack.translate(0.0F, 0.05F, finalZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case BACK -> {
                poseStack.translate(0.0F, 0.05F, finalZ);
            }
        }
    }
}
