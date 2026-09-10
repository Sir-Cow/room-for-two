package sircow.roomfortwo.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.*;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mod.EventBusSubscriber(modid = "roomfortwo", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetworkRegistry {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(Constants.id("main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions((status, version) -> version == PROTOCOL_VERSION)
            .serverAcceptedVersions((status, version) -> version == PROTOCOL_VERSION)
            .simpleChannel();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        INSTANCE.messageBuilder(BedOccupancySyncPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BedOccupancySyncPayload::write)
                .decoder(BedOccupancySyncPayload::new)
                .consumerMainThread(ForgeNetworkRegistry::handleClientPayload)
                .add();
    }

    private static void handleClientPayload(BedOccupancySyncPayload payload, CustomPayloadEvent.Context context) {
        Minecraft.getInstance().execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots()));
    }
}
