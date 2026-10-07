package sircow.roomfortwo.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sircow.roomfortwo.util.BedOccupancyTracker;

public class BedOccupancySyncHandler implements IMessageHandler<BedOccupancySyncPayload, IMessage> {
    @Override
    public IMessage onMessage(BedOccupancySyncPayload message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> BedOccupancyTracker.updateClientCache(message.getEntitySlots(), message.getEntitySleepPositions()));
        return null;
    }
}
