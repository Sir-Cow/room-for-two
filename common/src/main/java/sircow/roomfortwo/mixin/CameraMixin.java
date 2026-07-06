package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.platform.Services;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.Objects;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Quaternionf rotation;
    @Shadow private Entity entity;
    @Shadow protected abstract void move(float forwards, float up, float right);

    @Inject(method = "setup", at = @At("TAIL"))
    private void roomfortwo$adjustSleepCamera(BlockGetter blockGetter, Entity entity, boolean detached, boolean inverseView, float partialTick, CallbackInfo ci) {
        if (Objects.equals(Services.PLATFORM.getPlatformName(), "Forge")) return;
        if (!(this.entity instanceof LivingEntity living)) return;
        if (!living.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Direction dir = living.getBedOrientation();
        if (dir == null) return;

        float baseYaw = dir.toYRot();
        int slot = BedOccupancyTracker.getSlot(this.entity.getId());

        rotation.identity();
        rotation.rotateY((float) Math.toRadians(baseYaw));

        if (slot % 2 == 0) rotation.rotateZ((float) Math.toRadians(-90.0));
        else rotation.rotateZ((float) Math.toRadians(90.0));

        if (dir == Direction.NORTH || dir == Direction.SOUTH) rotation.rotateX((float) Math.toRadians(90.0));
        if (dir == Direction.EAST || dir == Direction.WEST) rotation.rotateX((float) Math.toRadians(-90.0));

        float verticalOffset = ((float) slot / 2) * 0.25F;
        move(-0.75F, 0.1F, 0.0F - verticalOffset);
    }
}
