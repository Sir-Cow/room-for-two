package sircow.roomfortwo.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.trigger.ModTriggers;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow public abstract Optional<BlockPos> getSleepingPos();

    @Unique private Vec3 roomfortwo$preSleepPos;

    @Inject(method = "startSleeping", at = @At("HEAD"))
    private void roomfortwo$onStartSleepingHead(BlockPos pos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        this.roomfortwo$preSleepPos = self.position();
    }

    @Inject(method = "startSleeping", at = @At("TAIL"))
    private void roomfortwo$onStartSleepingTail(BlockPos pos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide() && self.level() instanceof ServerLevel serverLevel) {
            BedOccupancyTracker.updateBedOccupancy(serverLevel, pos, -1, self.getId(), this.roomfortwo$preSleepPos);
            this.roomfortwo$preSleepPos = null;

            int count = BedOccupancyTracker.getOccupantCount(serverLevel, pos);
            if (count >= 2) {
                for (LivingEntity occupant : BedOccupancyTracker.getOccupants(serverLevel, pos)) {
                    if (occupant instanceof ServerPlayer occupantPlayer) {
                        ModTriggers.BED_OCCUPANCY.trigger(occupantPlayer);
                    }
                }
            }
        }
    }

    @Inject(method = "stopSleeping", at = @At("HEAD"))
    private void roomfortwo$onStopSleeping(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide() && self.level() instanceof ServerLevel serverLevel) {
            getSleepingPos().ifPresent(pos -> BedOccupancyTracker.updateBedOccupancy(serverLevel, pos, self.getId(), -1, null));
        }
    }
}
