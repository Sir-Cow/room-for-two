package sircow.roomfortwo.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(RenderPlayer.class)
public class RenderPlayerMixin {
    @Inject(method = "applyRotations(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    private void roomfortwo$rotateSleepingPlayers(AbstractClientPlayer entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (!entityLiving.isPlayerSleeping()) return;

        int id = entityLiving.getEntityId();
        int slot = BedOccupancyTracker.getSlot(id);
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(id);

        float baseZ = -0.15F;
        float zOffset = -((float) (slot / 2)) * 0.4F;
        float finalZ = baseZ + zOffset;

        Minecraft mc = Minecraft.getMinecraft();

        boolean firstPerson = mc.gameSettings.thirdPersonView == 0;
        boolean localPlayer = entityLiving == mc.player;

        switch (sleepPos) {
            case LEFT:
                GlStateManager.translate(0.25F, 0.0F, finalZ);
                if (firstPerson && localPlayer) GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                else GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case RIGHT:
                GlStateManager.translate(-0.25F, 0.0F, finalZ);
                if (firstPerson && localPlayer) GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                else GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case FRONT:
                GlStateManager.translate(0.0F, 0.05F, finalZ);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case BACK:
                GlStateManager.translate(0.0F, 0.05F, finalZ);
                break;
            case NONE:
            default:
                if (slot % 2 == 0) GlStateManager.translate(-0.25F, 0.0F, finalZ);
                else GlStateManager.translate(0.25F, 0.0F, finalZ);

                if (firstPerson && localPlayer) {
                    if (slot % 2 == 0) GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    else GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }
                else {
                    if (slot % 2 == 0) GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    else GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }
                break;
        }
    }
}
