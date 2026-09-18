package sircow.roomfortwo.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.Instance> {
    @Override
    public @NonNull Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static final class Instance implements SimpleInstance {
        public static final Codec<Instance> CODEC = MapCodec.unitCodec(new Instance());

        @Override
        public @NonNull Optional<Holder<LootItemCondition>> player() {
            return Optional.empty();
        }
    }
}
