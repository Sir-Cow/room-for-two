package sircow.roomfortwo.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import sircow.roomfortwo.Constants;
import sircow.roomfortwo.util.BedOccupancyTracker;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetworkRegistry {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            Constants.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        INSTANCE.registerMessage(
                0,
                BedOccupancySyncPayload.class,
                BedOccupancySyncPayload::encode,
                BedOccupancySyncPayload::new,
                ForgeNetworkRegistry::handleClientPayload
        );
    }

    private static void handleClientPayload(BedOccupancySyncPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> Minecraft.getInstance().execute(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots())));
        context.setPacketHandled(true);
    }
}
