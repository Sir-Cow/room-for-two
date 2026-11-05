package sircow.roomfortwo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sircow.roomfortwo.platform.Services;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"))
    private void roomfortwo$keepRepinnedOccupant(ServerPlayer player, double x, double y, double z, float yRot, float xRot, Operation<Void> original) {
        if (!player.isSleeping() || !Services.PLATFORM.isModLoaded("sable")) {
            original.call(player, x, y, z, yRot, xRot);
        }
    }
}
