package sircow.roomfortwo.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import sircow.roomfortwo.roomfortwo.Tags;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("RoomForTwo")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class RoomForTwoLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(String.format("mixins.%s.json", Tags.MOD_ID));
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
