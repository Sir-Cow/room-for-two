package sircow.roomfortwo.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$kickFromBed(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Villager villager = (Villager) (Object) this;
        if (!villager.isSleeping()) return;
        if (villager.level.isClientSide) return;

        villager.stopSleeping();
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void roomfortwo$recordApproachPosition(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;

        if (!(villager.getLevel() instanceof ServerLevel serverLevel)) return;
        if (villager.isSleeping()) return;

        BedOccupancyTracker.recordVillagerApproach(serverLevel, villager);
    }
}
