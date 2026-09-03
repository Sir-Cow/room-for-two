package sircow.roomfortwo.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import sircow.roomfortwo.util.BedOccupancyTracker;

@EventBusSubscriber(modid = "roomfortwo")
public class NeoForgeNetworkRegistry {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                BedOccupancySyncPayload.TYPE,
                BedOccupancySyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> BedOccupancyTracker.updateClientCache(payload.entitySlots(), payload.entitySleepPositions()))
        );
    }
}
