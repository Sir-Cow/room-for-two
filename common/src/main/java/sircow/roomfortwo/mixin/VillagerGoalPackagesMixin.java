package sircow.roomfortwo.mixin;

import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {
    @Redirect(method = "validateBedPoi", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable<?> roomfortwo$ignoreOccupiedBed(BlockState state, Property<?> property) {
        return false;
    }
}
