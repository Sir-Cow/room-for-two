package sircow.roomfortwo.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mixin(AbstractBedBlock.class)
public class AbstractBedBlockMixin {
    @Redirect(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 4))
    private Comparable<?> roomfortwo$use(BlockState state, Property<?> property) {
        return false;
    }

    @Inject(method = "onStopSleeping", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$preventDestroyWithOccupants(Level level, BlockPos blockPos, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            int count = BedOccupancyTracker.getOccupantCount(serverLevel, blockPos);

            if (count > 0) {
                ci.cancel();
            }
        }
    }
}
