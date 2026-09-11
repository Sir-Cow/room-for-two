package sircow.roomfortwo.trigger;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricModTriggers {
    public static void registerFabricModTriggers() {
        ModTriggers.getTriggers().forEach((id, trigger) ->
                Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger)
        );
    }
}
