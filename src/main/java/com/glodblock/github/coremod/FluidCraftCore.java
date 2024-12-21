package com.glodblock.github.coremod;

import java.util.Map;

import javax.annotation.Nullable;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("FluidCraftCore")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("com.glodblock.github.coremod")
public class FluidCraftCore implements IFMLLoadingPlugin {

    private static final boolean DUMP_CLASSES = Boolean.parseBoolean(System.getProperty("ae2fc.dumpClass", "false"));
    private static boolean OBF_ENV;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { FCClassTransformer.class.getName() };
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        OBF_ENV = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static boolean DUMP_CLASSES() {
        return DUMP_CLASSES || !OBF_ENV;
    }
}
