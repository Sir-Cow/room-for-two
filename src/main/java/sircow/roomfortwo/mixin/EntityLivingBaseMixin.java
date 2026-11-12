package sircow.roomfortwo.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin {
    @Redirect(method = "collideWithNearbyEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean roomfortwo$preventCrammingWhileInBed(EntityLivingBase self, DamageSource source, float amount) {
        return !self.isPlayerSleeping() && self.attackEntityFrom(source, amount);
    }
}
