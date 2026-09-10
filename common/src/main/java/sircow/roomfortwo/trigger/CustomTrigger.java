package sircow.roomfortwo.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.Instance> {
    @Override
    public @NotNull Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static final class Instance implements SimpleInstance {
        public static final Codec<Instance> CODEC = MapCodec.unit(new Instance()).codec();

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
