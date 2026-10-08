package sircow.roomfortwo.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import sircow.roomfortwo.Constants;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Constants.id("bed_occupancy");

    @Override
    protected @NotNull TriggerInstance createInstance(@NotNull JsonObject json, EntityPredicate.@NotNull Composite player, @NotNull DeserializationContext context) {
        return new TriggerInstance(player);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(EntityPredicate.Composite player) {
            super(ID, player);
        }
    }
}
