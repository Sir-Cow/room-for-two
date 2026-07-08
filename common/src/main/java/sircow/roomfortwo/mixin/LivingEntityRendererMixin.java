package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void roomfortwo$rotateSleepingEntities(LivingEntity livingEntity, PoseStack poseStack, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci) {
        if (livingEntity.getPose() != Pose.SLEEPING) return;
        if (!livingEntity.isSleeping()) return;

        int slot = BedOccupancyTracker.getSlot(livingEntity.getId());
        float baseZ = -0.15F;

        float zOffset = -(float) (slot / 2) * 0.4F;
        float finalZ = baseZ + zOffset;

        if (slot % 2 == 0) poseStack.translate(-0.25F, 0.0F, finalZ);
        else poseStack.translate(0.25F, 0.0F, finalZ);

        Minecraft minecraft = Minecraft.getInstance();

        boolean firstPerson = minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
        boolean localPlayer = livingEntity == minecraft.player;

        if (firstPerson && localPlayer) {
            if (slot % 2 == 0) poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            else poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        }
        else {
            if (slot % 2 == 0) poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            else poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        }
    }
}
