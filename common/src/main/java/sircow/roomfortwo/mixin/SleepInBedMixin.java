package sircow.roomfortwo.mixin;

import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SleepInBed.class)
public class SleepInBedMixin {
    @Redirect(method = "checkExtraStartConditions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable<?> roomfortwo$ignoreOccupiedBed(BlockState state, Property<?> property) {
        return false;
    }
}
