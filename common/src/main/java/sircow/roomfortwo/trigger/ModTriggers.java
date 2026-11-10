package sircow.roomfortwo.trigger;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import sircow.roomfortwo.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModTriggers {
    private static final Map<ResourceLocation, CriterionTrigger<?>> TRIGGERS = new LinkedHashMap<>();

    public static final CustomTrigger BED_OCCUPANCY = register("bed_occupancy", new CustomTrigger());

    private static <T extends CriterionTrigger<?>> T register(String name, T trigger) {
        TRIGGERS.put(Constants.id(name), trigger);
        return trigger;
    }

    public static Map<ResourceLocation, CriterionTrigger<?>> getTriggers() {
        return TRIGGERS;
    }
}
