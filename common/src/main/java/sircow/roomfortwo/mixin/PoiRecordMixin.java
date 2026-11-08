package sircow.roomfortwo.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoiRecord.class)
public class PoiRecordMixin {
    @Shadow @Final private Holder<PoiType> poiType;

    @Inject(method = "hasSpace", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$uncapHomePoi(CallbackInfoReturnable<Boolean> cir) {
        if (this.poiType.is(PoiTypes.HOME)) {
            cir.setReturnValue(true);
        }
    }
}
