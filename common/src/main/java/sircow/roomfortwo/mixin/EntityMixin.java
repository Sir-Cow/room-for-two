package sircow.roomfortwo.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getBoundingBox", at = @At("TAIL"), cancellable = true)
    private void roomfortwo$getExpandedSleepingBox(CallbackInfoReturnable<AABB> cir) {
        if ((Object) this instanceof Villager villager) {
            if (!villager.isSleeping()) return;

            AABB modifiedBox = cir.getReturnValue().inflate(0.175D, 0.075D, 0.175D);

            Direction sleepingDirection = villager.getBedOrientation();
            if (sleepingDirection == null || !sleepingDirection.getAxis().isHorizontal()) return;

            Direction sidewaysDirection = sleepingDirection.getCounterClockWise();

            cir.setReturnValue(modifiedBox.move((sleepingDirection.getStepX() * 0.2D) + (sidewaysDirection.getStepX() * -0.2D), 0.2D, (sleepingDirection.getStepZ() * 0.2D) + (sidewaysDirection.getStepZ() * -0.2D)));
        }
    }
}