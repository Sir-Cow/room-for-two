package sircow.roomfortwo.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$kickFromBed(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Villager villager = (Villager) (Object) this;
        if (!villager.isSleeping()) return;
        if (villager.getLevel().isClientSide()) return;

        villager.stopSleeping();
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
