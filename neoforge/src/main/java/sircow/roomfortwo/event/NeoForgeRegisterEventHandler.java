package sircow.roomfortwo.event;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.trigger.ModTriggers;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeRegisterEventHandler {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, helper ->
                ModTriggers.getTriggers().forEach(helper::register)
        );
    }
}
