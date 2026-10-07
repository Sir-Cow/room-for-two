package sircow.roomfortwo;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sircow.roomfortwo.client.EntityViewRenderHandler;
import sircow.roomfortwo.command.BedCheckCommand;
import sircow.roomfortwo.command.SleepPosCommand;
import sircow.roomfortwo.network.RoomForTwoNetwork;
import sircow.roomfortwo.roomfortwo.Tags;
import sircow.roomfortwo.trigger.ModTriggers;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class RoomForTwo {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG.info("Initialising {}", Tags.MOD_NAME);
        RoomForTwoNetwork.init();
        ModTriggers.getTriggers().forEach((id, trigger) -> CriteriaTriggers.register(trigger));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new EntityViewRenderHandler());
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new SleepPosCommand());
        event.registerServerCommand(new BedCheckCommand());
    }
}