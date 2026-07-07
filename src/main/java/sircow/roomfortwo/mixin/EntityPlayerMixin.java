package sircow.roomfortwo.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
    @Unique
    private Vec3d roomfortwo$preSleepPos;

    @Inject(method = "trySleep", at = @At("HEAD"))
    private void roomfortwo$onStartSleepingHead(BlockPos bedLocation, CallbackInfoReturnable<EntityPlayer.SleepResult> cir) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        this.roomfortwo$preSleepPos = self.getPositionVector();
    }

    @Inject(method = "trySleep", at = @At("RETURN"))
    private void roomfortwo$onStartSleeping(BlockPos bedLocation, CallbackInfoReturnable<EntityPlayer.SleepResult> cir) {
        if (cir.getReturnValue() != EntityPlayer.SleepResult.OK) return;

        EntityPlayer self = (EntityPlayer) (Object) this;
        if (!(self.world instanceof WorldServer)) return;

        BedOccupancyTracker.updateBedOccupancy((WorldServer) self.world, bedLocation, -1, self.getEntityId(), this.roomfortwo$preSleepPos);
        this.roomfortwo$preSleepPos = null;
    }

    @Inject(method = "wakeUpPlayer", at = @At("HEAD"))
    private void roomfortwo$onStopSleeping(boolean immediately, boolean updateWorldFlag, boolean setSpawn, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        if (!(self.world instanceof WorldServer)) return;
        BlockPos bedPos = self.bedLocation;

        if (bedPos != null) BedOccupancyTracker.updateBedOccupancy((WorldServer) self.world, bedPos, self.getEntityId(), -1, null);
    }
}
