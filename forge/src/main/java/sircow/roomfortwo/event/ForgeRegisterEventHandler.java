package sircow.roomfortwo.event;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.trigger.ModTriggers;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeRegisterEventHandler {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, helper ->
                ModTriggers.getTriggers().forEach(helper::register)
        );
    }
}
