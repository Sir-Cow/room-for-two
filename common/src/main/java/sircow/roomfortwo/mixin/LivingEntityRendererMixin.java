package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.interfaces.RoomForTwoRenderState;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void roomfortwo$rotateSleepingEntities(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        if (!state.hasPose(Pose.SLEEPING)) return;
        if (!(state instanceof RoomForTwoRenderState slotHolder)) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        int id = slotHolder.roomfortwo$getEntityId();
        if (!(level.getEntity(id) instanceof LivingEntity self)) return;
        if (!self.isSleeping()) return;

        int slot = BedOccupancyTracker.getSlot(id);
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(id);
        boolean firstPerson = Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
        boolean localPlayer = self == Minecraft.getInstance().player;
        float baseZ = -0.15F;
        float zOffset = -(float) (slot / 2) * 0.4F;
        float finalZ = baseZ + zOffset;

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
