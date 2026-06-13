package com.glodblock.github.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

import com.glodblock.github.inventory.IDualHost;

public final class DualHostSettings {

    private static final String FLUID_CONFIG_KEY = "fluidConfig";

    private DualHostSettings() {}

    public static void uploadSettings(IDualHost host, NBTTagCompound compound) {
        if (compound.hasKey(FLUID_CONFIG_KEY, NBT.TAG_COMPOUND)) {
            host.getDualityFluid().readConfigFromNBT(compound, FLUID_CONFIG_KEY);
            host.getConfig();
        }
    }

    public static void downloadSettings(IDualHost host, NBTTagCompound output) {
        host.getDualityFluid().writeConfigToNBT(output, FLUID_CONFIG_KEY);
    }

    public static void copySettings(IDualHost target, IDualHost source) {
        NBTTagCompound data = new NBTTagCompound();
        downloadSettings(source, data);
        uploadSettings(target, data);
    }
}
