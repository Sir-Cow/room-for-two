package sircow.roomfortwo.trigger;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.util.ResourceLocation;
import sircow.roomfortwo.roomfortwo.Tags;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModTriggers {
    private static final Map<ResourceLocation, ICriterionTrigger<?>> TRIGGERS = new LinkedHashMap<>();
    public static final CustomTrigger BED_OCCUPANCY = register("bed_occupancy", new CustomTrigger());

    private ModTriggers() {}

    private static <T extends ICriterionTrigger<?>> T register(String name, T trigger) {
        TRIGGERS.put(new ResourceLocation(Tags.MOD_ID, name), trigger);
        return trigger;
    }

    public static Map<ResourceLocation, ICriterionTrigger<?>> getTriggers() {
        return Collections.unmodifiableMap(TRIGGERS);
    }
}
