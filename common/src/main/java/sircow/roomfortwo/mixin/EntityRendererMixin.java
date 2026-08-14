package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$hideLocalPlayerWhileSleeping(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player && entity.hasPose(Pose.SLEEPING)) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null && entity.getId() == localPlayer.getId() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$removeOtherPlayerNametagsWhileSleeping(Entity entity, Component component, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (entity instanceof Player && entity.hasPose(Pose.SLEEPING)) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null && entity.getId() != localPlayer.getId()) {
                ci.cancel();
            }
        }
    }
}
