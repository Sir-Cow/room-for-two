package sircow.roomfortwo.mixin;

import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoiRecord.class)
public class PoiRecordMixin {
    @Shadow @Final private PoiType poiType;

    @Inject(method = "hasSpace", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$uncapHomePoi(CallbackInfoReturnable<Boolean> cir) {
        if (this.poiType == PoiType.HOME) {
            cir.setReturnValue(true);
        }
    }
}
