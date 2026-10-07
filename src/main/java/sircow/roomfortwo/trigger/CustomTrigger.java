package sircow.roomfortwo.trigger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomTrigger implements ICriterionTrigger<CustomTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("roomfortwo", "bed_occupancy");
    private final Map<PlayerAdvancements, CustomTrigger.Listeners> listeners = Maps.newHashMap();

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(@NotNull PlayerAdvancements playerAdvancements, ICriterionTrigger.@NotNull Listener<TriggerInstance> listener) {
        CustomTrigger.Listeners list = this.listeners.get(playerAdvancements);

        if (list == null) {
            list = new CustomTrigger.Listeners(playerAdvancements);
            this.listeners.put(playerAdvancements, list);
        }

        list.add(listener);
    }

    @Override
    public void removeListener(@NotNull PlayerAdvancements playerAdvancements, ICriterionTrigger.@NotNull Listener<TriggerInstance> listener) {
        CustomTrigger.Listeners list = this.listeners.get(playerAdvancements);

        if (list != null) {
            list.remove(listener);

            if (list.isEmpty()) this.listeners.remove(playerAdvancements);
        }
    }

    @Override
    public void removeAllListeners(@NotNull PlayerAdvancements playerAdvancements) {
        this.listeners.remove(playerAdvancements);
    }

    @Override
    public @NotNull TriggerInstance deserializeInstance(@NotNull JsonObject json, @NotNull JsonDeserializationContext context) {
        return new TriggerInstance(this.getId());
    }

    public void trigger(EntityPlayerMP player) {
        CustomTrigger.Listeners list = this.listeners.get(player.getAdvancements());

        if (list != null) list.trigger();
    }

    public static class TriggerInstance extends AbstractCriterionInstance {
        public TriggerInstance(ResourceLocation criterionIn) {
            super(criterionIn);
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<TriggerInstance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<TriggerInstance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<TriggerInstance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            List<ICriterionTrigger.Listener<TriggerInstance>> granted = null;

            for (ICriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (granted == null) granted = Lists.newArrayList();

                granted.add(listener);
            }

            if (granted != null) {
                for (ICriterionTrigger.Listener<TriggerInstance> listener : granted) {
                    listener.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}
