package sircow.roomfortwo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
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
    @Unique private boolean roomfortwo$wasSleeping;

    @Invoker("setPosToBed")
    protected abstract void roomfortwo$callSetPosToBed(BlockPos pos);

    @Inject(method = "startSleeping", at = @At("HEAD"))
    private void roomfortwo$onStartSleepingHead(BlockPos pos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        this.roomfortwo$preSleepPos = self.position();
        this.roomfortwo$wasSleeping = self.isSleeping();
    }

    @Inject(method = "startSleeping", at = @At("TAIL"))
    private void roomfortwo$onStartSleepingTail(BlockPos pos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Vec3 enteringPos = this.roomfortwo$preSleepPos;
        this.roomfortwo$preSleepPos = null;

        if (this.roomfortwo$wasSleeping) return;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;

        if (self instanceof Villager) enteringPos = BedOccupancyTracker.takeVillagerApproachPos(self.getId(), pos, enteringPos);

        BedOccupancyTracker.updateBedOccupancy(serverLevel, pos, -1, self.getId(), enteringPos);

        if (BedOccupancyTracker.getOccupantCount(serverLevel, pos) >= 2) {
            for (LivingEntity occupant : BedOccupancyTracker.getOccupants(serverLevel, pos)) {
                if (occupant instanceof ServerPlayer occupantPlayer) ModTriggers.BED_OCCUPANCY.trigger(occupantPlayer);
            }
        }
    }

    @Inject(method = "stopSleeping", at = @At("HEAD"))
    private void roomfortwo$onStopSleepingHead(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;

        BedOccupancyTracker.setCurrentlyLeaving(self.getId());
        self.getSleepingPos().ifPresent(pos ->
                BedOccupancyTracker.updateBedOccupancy(serverLevel, pos, self.getId(), -1, null)
        );
    }

    @Inject(method = "stopSleeping", at = @At("TAIL"))
    private void roomfortwo$onStopSleepingTail(CallbackInfo ci) {
        BedOccupancyTracker.clearCurrentlyLeaving();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void roomfortwo$repinOccupantToMovedBed(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.isSleeping()) return;
        if (self.level().isClientSide() && !(self instanceof Player player && player.isLocalPlayer())) return;

        self.getSleepingPos().ifPresent(this::roomfortwo$callSetPosToBed);
    }

    // store occupancy slot so it persists on leave + rejoin
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void roomfortwo$saveBedOccupantSlot(ValueOutput output, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.isSleeping()) return;

        Optional<BlockPos> sleepingPos = self.getSleepingPos();
        if (sleepingPos.isEmpty()) return;

        int slot = BedOccupancyTracker.getServerSlot(self.level(), sleepingPos.get(), self.getId());
        if (slot >= 0) output.putInt("roomfortwo$bedSlot", slot);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void roomfortwo$restoreBedOccupantSlot(ValueInput input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self.level() instanceof ServerLevel serverLevel)) return;

        Optional<Integer> slot = input.getInt("roomfortwo$bedSlot");
        if (slot.isEmpty() || !self.isSleeping()) return;

        int bedSlot = slot.get();
        getSleepingPos().ifPresent(pos -> BedOccupancyTracker.restoreOccupant(serverLevel, pos, self.getId(), bedSlot));
    }

    @WrapOperation(method = "pushEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean roomfortwo$preventCrammingWhileInBed(LivingEntity self, ServerLevel level, DamageSource source, float amount, Operation<Boolean> original) {
        return !self.isSleeping() && original.call(self, level, source, amount);
    }
}
