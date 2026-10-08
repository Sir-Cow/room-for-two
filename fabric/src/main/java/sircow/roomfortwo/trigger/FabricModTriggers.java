package sircow.roomfortwo.trigger;

import net.minecraft.advancements.CriteriaTriggers;

public class FabricModTriggers {
    public static void registerFabricModTriggers() {
        ModTriggers.getTriggers().forEach((id, trigger) ->
                CriteriaTriggers.register(trigger)
        );
    }
}
