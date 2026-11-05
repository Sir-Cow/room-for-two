package sircow.roomfortwo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import sircow.roomfortwo.platform.Services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SableInterop {
    private static Boolean present;
    private static boolean unsupported, renderUnsupported;
    private static Object helper;
    private static Method getContaining, logicalPoseMethod, renderPoseMethod, transformPos, transformPosInv, orientationMethod;
    private static Class<?> clientSubLevelClass;

    private SableInterop() {}

    public static boolean isPresent() {
        if (present == null) present = Services.PLATFORM.isModLoaded("sable");
        return present;
    }

    public static Vec3 toBedSpace(Level level, BlockPos bedPos, Vec3 worldPos) {
        Object pose = logicalPose(level, bedPos);

        if (pose == null) return worldPos;

        try {
            return (Vec3) transformPosInv.invoke(pose, worldPos);
        }
        catch (ReflectiveOperationException | LinkageError e) {
            unsupported = true;
            return worldPos;
        }
    }

    public static Vec3 toWorldSpace(Level level, BlockPos bedPos, Vec3 bedSpacePos) {
        Object pose = logicalPose(level, bedPos);

        if (pose == null) return bedSpacePos;

        try {
            return (Vec3) transformPos.invoke(pose, bedSpacePos);
        }
        catch (ReflectiveOperationException | LinkageError e) {
            unsupported = true;
            return bedSpacePos;
        }
    }

    public static Quaternionf renderOrientation(Level level, BlockPos bedPos) {
        if (!isPresent() || unsupported || renderUnsupported) return null;

        try {
            Object subLevel = subLevel(level, bedPos);

            if (subLevel == null) return null;
            if (clientSubLevelClass == null) clientSubLevelClass = Class.forName("dev.ryanhcode.sable.sublevel.ClientSubLevel");
            if (!clientSubLevelClass.isInstance(subLevel)) return null;
            if (renderPoseMethod == null) renderPoseMethod = subLevel.getClass().getMethod("renderPose");

            Object pose = renderPoseMethod.invoke(subLevel);

            if (pose == null) return null;
            if (orientationMethod == null) orientationMethod = pose.getClass().getMethod("orientation");

            Object orientation = orientationMethod.invoke(pose);

            if (orientation instanceof Quaterniondc quaternion) {
                return new Quaternionf(quaternion);
            }
            return null;
        }
        catch (ReflectiveOperationException | LinkageError e) {
            renderUnsupported = true;
            return null;
        }
    }

    private static Object subLevel(Level level, BlockPos bedPos) {
        if (!isPresent() || unsupported) return null;

        try {
            if (getContaining == null) {
                Field helperField = Class.forName("dev.ryanhcode.sable.Sable").getField("HELPER");
                helper = helperField.get(null);
                getContaining = helper.getClass().getMethod("getContaining", Level.class, Vec3i.class);
            }
            return getContaining.invoke(helper, level, bedPos);
        }
        catch (ReflectiveOperationException | LinkageError e) {
            unsupported = true;
            return null;
        }
    }

    private static Object logicalPose(Level level, BlockPos bedPos) {
        Object subLevel = subLevel(level, bedPos);

        if (subLevel == null) return null;

        try {
            if (logicalPoseMethod == null) {
                logicalPoseMethod = subLevel.getClass().getMethod("logicalPose");
                Class<?> poseClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");
                transformPos = poseClass.getMethod("transformPosition", Vec3.class);
                transformPosInv = poseClass.getMethod("transformPositionInverse", Vec3.class);
            }
            return logicalPoseMethod.invoke(subLevel);
        }
        catch (ReflectiveOperationException | LinkageError e) {
            unsupported = true;
            return null;
        }
    }
}
