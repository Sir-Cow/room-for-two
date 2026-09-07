package sircow.roomfortwo.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void roomfortwo$readSleepPosition(ValueInput input, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        String saved = input.getStringOr("roomfortwo$sleepPosition", "none");
        SleepPosition position = SleepPosition.fromString(saved);

        if (position != null) BedOccupancyTracker.setServerSleepPos(self.getId(), position);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void roomfortwo$writeSleepPosition(ValueOutput output, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        SleepPosition position = BedOccupancyTracker.getServerSleepPos(self.getId());

        output.putString("roomfortwo$sleepPosition", position.getName());
    }
}
