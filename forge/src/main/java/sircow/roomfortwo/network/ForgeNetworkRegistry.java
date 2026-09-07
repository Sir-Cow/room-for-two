package sircow.roomfortwo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkProtocol;
import net.minecraftforge.network.SimpleChannel;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.util.BedOccupancyTracker;

@Mod.EventBusSubscriber(modid = "roomfortwo", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetworkRegistry {
    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(Constants.id("main"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        StreamCodec<RegistryFriendlyByteBuf, BedOccupancySyncPayload> castedCodec = (StreamCodec<RegistryFriendlyByteBuf, BedOccupancySyncPayload>) (Object) BedOccupancySyncPayload.STREAM_CODEC;
        INSTANCE.protocol(NetworkProtocol.PLAY).flow(PacketFlow.CLIENTBOUND).add(BedOccupancySyncPayload.class, castedCodec, ForgeNetworkRegistry::handleClientPayload);
        INSTANCE.build();
    }

    private static void handleClientPayload(BedOccupancySyncPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> Minecraft.getInstance().execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots(), payload.entitySleepPositions())));
        context.setPacketHandled(true);
    }
}
