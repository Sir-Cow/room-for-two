package sircow.roomfortwo.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sircow.roomfortwo.interfaces.RoomForTwoRenderState;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

import java.util.Optional;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Unique private static final int roomfortwo$occupantRenderMaxCount = 64;
    @Unique private static final double roomfortwo$occupantRenderDist = 32.0D;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$hideLocalPlayerWhileSleeping(Entity entity, Frustum culler, double x, double y, double z, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player && entity.hasPose(Pose.SLEEPING)) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null && entity.getId() == localPlayer.getId()) {
                if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$renderBedOccupants(Entity entity, Frustum culler, double x, double y, double z, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof LivingEntity sleeper) || !sleeper.isSleeping()) return;

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && entity.getId() == localPlayer.getId() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Optional<BlockPos> sleepingPos = sleeper.getSleepingPos();
        if (sleepingPos.isEmpty()) return;

        BlockPos bedPos = sleepingPos.get();
        int side = roomfortwo$sideOf(sleeper);
        int slot = BedOccupancyTracker.getSlot(sleeper.getId());
        int rank = 0;

        // determine what side an occupant is on and cap amount rendered
        for (LivingEntity occupant : entity.level().getEntitiesOfClass(LivingEntity.class, BedOccupancyTracker.bedBox(entity.level(), bedPos), other -> other.isSleeping() && other.getSleepingPos().map(bedPos::equals).orElse(false))) {
            if (occupant.getId() != entity.getId() && roomfortwo$sideOf(occupant) == side && BedOccupancyTracker.getSlot(occupant.getId()) < slot) {
                rank++;
            }
        }

        if (rank >= roomfortwo$occupantRenderMaxCount / 2) {
            cir.setReturnValue(false);
            return;
        }

        // render occupants when bed isn't visible
        Vec3 bedCenter = BedOccupancyTracker.bedWorldCentre(entity.level(), bedPos);
        double dx = x - bedCenter.x;
        double dy = y - bedCenter.y;
        double dz = z - bedCenter.z;

        if (dx * dx + dy * dy + dz * dz <= roomfortwo$occupantRenderDist * roomfortwo$occupantRenderDist) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static int roomfortwo$sideOf(LivingEntity occupant) {
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(occupant.getId());

        if (sleepPos == SleepPosition.LEFT) return 1;
        if (sleepPos == SleepPosition.RIGHT) return 0;

        return BedOccupancyTracker.getSlot(occupant.getId()) % 2;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void roomfortwo$removeOtherPlayerNametagsWhileSleeping(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player && entity.hasPose(Pose.SLEEPING)) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null && entity.getId() != localPlayer.getId()) state.nameTag = null;
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void roomfortwo$storeEntityId(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo ci) {
        ((RoomForTwoRenderState) state).roomfortwo$setEntityId(entity.getId());
    }
}
