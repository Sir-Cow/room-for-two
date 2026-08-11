package sircow.roomfortwo.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sircow.roomfortwo.interfaces.RoomForTwoRenderState;

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

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void roomfortwo$removeOtherPlayerNametagsWhileSleeping(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player && entity.hasPose(Pose.SLEEPING)) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null && entity.getId() != localPlayer.getId()) state.nameTag = null;
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void roomfortwo$storeEntityId(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
        ((RoomForTwoRenderState) state).roomfortwo$setEntityId(entity.getId());
    }
}
