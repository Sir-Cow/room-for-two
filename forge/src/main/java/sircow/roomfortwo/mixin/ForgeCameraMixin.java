package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mixin(Camera.class)
public abstract class ForgeCameraMixin {
    @Shadow private Level level;
    @Shadow private Entity entity;
    @Shadow private boolean detached, initialized;
    @Shadow private float partialTickTime, eyeHeight;

    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void move(float forwards, float up, float right);

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$forceStaticSleepCamera(Level level, Entity entity, boolean detached, boolean inverseView, float partialTick, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                this.initialized = true;
                this.level = level;
                this.entity = entity;
                this.detached = detached;
                this.partialTickTime = partialTick;

                this.setPosition(livingEntity.getX(), livingEntity.getY() + (double) this.eyeHeight, livingEntity.getZ());

                int slot = BedOccupancyTracker.getSlot(this.entity.getId());
                float verticalOffset = ((float) slot / 2) * 0.25F;
                move(-0.5F, 0.1F, 0.0F - verticalOffset);
                ci.cancel();
            }
        }
    }
}
