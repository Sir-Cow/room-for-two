package sircow.roomfortwo.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ValidateNearbyPoi.class)
public class ValidateNearbyPoiMixin {
    @Inject(method = "bedIsOccupied", at = @At("HEAD"), cancellable = true)
    private static void roomfortwo$bedNeverOccupied(ServerLevel level, BlockPos pos, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
