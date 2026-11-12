package sircow.roomfortwo.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(RenderManager.class)
public class RenderManagerMixin {
    @Unique private static final int roomfortwo$occupantRenderMaxCount = 64;
    @Unique private static final double roomfortwo$occupantRenderDist = 32.0D;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$hideLocalPlayerWhileSleeping(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!(entityIn instanceof EntityPlayer)) return;
        if (!((EntityPlayer) entityIn).isPlayerSleeping()) return;

        EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;

        if (localPlayer != null && entityIn == localPlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$renderBedOccupants(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!(entityIn instanceof EntityPlayer)) return;
        if (!((EntityPlayer) entityIn).isPlayerSleeping()) return;

        EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
        if (localPlayer != null && entityIn == localPlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) return;

        BlockPos bedPos = ((EntityPlayer) entityIn).bedLocation;
        if (bedPos == null) return;

        int side = roomfortwo$sideOf((EntityPlayer) entityIn);
        int slot = BedOccupancyTracker.getSlot(entityIn.getEntityId());
        int rank = 0;

        // determine what side an occupant is on and cap amount rendered
        for (EntityPlayer occupant : entityIn.world.getEntitiesWithinAABB(EntityPlayer.class, BedOccupancyTracker.bedBox(bedPos), other -> other.isPlayerSleeping() && bedPos.equals(other.bedLocation))) {
            if (occupant != entityIn && roomfortwo$sideOf(occupant) == side && BedOccupancyTracker.getSlot(occupant.getEntityId()) < slot) {
                rank++;
            }
        }

        if (rank >= roomfortwo$occupantRenderMaxCount / 2) {
            cir.setReturnValue(false);
            return;
        }

        // render occupants when bed isn't visible
        Vec3d bedCenter = BedOccupancyTracker.bedWorldCentre(bedPos);
        double dx = camX - bedCenter.x;
        double dy = camY - bedCenter.y;
        double dz = camZ - bedCenter.z;

        if (dx * dx + dy * dy + dz * dz <= roomfortwo$occupantRenderDist * roomfortwo$occupantRenderDist) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int roomfortwo$sideOf(EntityPlayer occupant) {
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(occupant.getEntityId());

        if (sleepPos == SleepPosition.LEFT) return 1;
        if (sleepPos == SleepPosition.RIGHT) return 0;

        return BedOccupancyTracker.getSlot(occupant.getEntityId()) % 2;
    }
}
