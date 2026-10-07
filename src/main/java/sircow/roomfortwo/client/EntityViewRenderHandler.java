package sircow.roomfortwo.client;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sircow.roomfortwo.util.BedOccupancyTracker;
import sircow.roomfortwo.util.SleepPosition;

public class EntityViewRenderHandler {
    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        if (player == null) return;
        if (!player.isPlayerSleeping()) return;
        if (mc.gameSettings.thirdPersonView != 0) return;

        BlockPos bedPos = player.bedLocation;
        if (bedPos == null) return;

        IBlockState state = player.world.getBlockState(bedPos);
        if (!(state.getBlock() instanceof BlockBed)) return;

        EnumFacing direction = state.getValue(BlockHorizontal.FACING);
        boolean northSouth = direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH;

        float baseYaw = direction.getHorizontalAngle() - 180.0F;
        int slot = BedOccupancyTracker.getSlot(player.getEntityId());
        SleepPosition sleepPos = BedOccupancyTracker.getSleepPos(player.getEntityId());

        float targetPitch, targetYaw, translateX, translateY;
        boolean evenSide;

        switch (sleepPos) {
            case LEFT:
                evenSide = true;
                break;
            case RIGHT:
                evenSide = false;
                break;
            case FRONT:
                applyCamera(event, player, -90.0F, -direction.getHorizontalAngle(), 0.0F, northSouth ? -90.0F : 90.0F, 0.0F, 0.0F);
                return;
            case BACK:
                applyCamera(event, player, -90.0F, -direction.getHorizontalAngle(), 180.0F, northSouth ? 90.0F : -90.0F, 0.0F, 0.0F);
                return;
            case NONE:
            default:
                evenSide = slot % 2 != 0;
                break;
        }

        if (evenSide) {
            targetPitch = -90.0F;
            targetYaw = baseYaw + 90.0F;
            translateX = -0.75F;
            translateY = -0.25F;
        }
        else {
            targetPitch = 90.0F;
            targetYaw = baseYaw - 90.0F;
            translateX = 0.75F;
            translateY = 0.25F;
        }

        applyCamera(event, player, targetPitch, targetYaw, -90.0F, -90.0F, translateX, translateY);
    }

    private void applyCamera(EntityViewRenderEvent.CameraSetup event, EntityPlayer player, float targetPitch, float targetYaw, float eventRoll, float targetRoll, float translateX, float translateY) {
        targetYaw = (targetYaw % 360.0F + 360.0F) % 360.0F;

        player.rotationYaw = targetYaw;
        player.prevRotationYaw = targetYaw;
        player.rotationPitch = targetPitch;
        player.prevRotationPitch = targetPitch;

        player.rotationYawHead = targetYaw;
        player.prevRotationYawHead = targetYaw;
        player.renderYawOffset = targetYaw;
        player.prevRenderYawOffset = targetYaw;

        event.setPitch(targetPitch);
        event.setYaw(targetYaw);
        event.setRoll(eventRoll);

        if (translateX != 0.0F || translateY != 0.0F) GlStateManager.translate(translateX, translateY, 0.0F);

        GlStateManager.rotate(targetRoll, 1.0F, 0.0F, 0.0F);
    }
}
