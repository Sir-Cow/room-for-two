package sircow.roomfortwo.mixin;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import sircow.roomfortwo.interfaces.RoomForTwoRenderState;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements RoomForTwoRenderState {
    @Unique private int roomfortwo$entityId;

    @Override
    public int roomfortwo$getEntityId() {
        return this.roomfortwo$entityId;
    }

    @Override
    public void roomfortwo$setEntityId(int id) {
        this.roomfortwo$entityId = id;
    }
}
