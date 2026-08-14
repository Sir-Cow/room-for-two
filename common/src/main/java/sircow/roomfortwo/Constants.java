package sircow.roomfortwo;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MOD_ID = "roomfortwo";
	public static final String MOD_NAME = "RoomForTwo";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static ResourceLocation id(String name) {
		return new ResourceLocation(MOD_ID, name);
	}
}
