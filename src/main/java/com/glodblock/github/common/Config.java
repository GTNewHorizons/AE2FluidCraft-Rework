package com.glodblock.github.common;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class Config {

    private static final Configuration Config = new Configuration(
            new File(new File((File) FMLInjectionData.data()[6], "config"), "ae2fc.cfg"));

    public static boolean fluidCells;
    public static boolean noFluidPacket;
    public static boolean fluidIOBus;
    public static boolean removeRecipe;
    public static double portableCellBattery;
    public static int packetSize;
    public static int packetRate;
    public static boolean replaceEC2;

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadProperty() {
        fluidCells = Config.getBoolean(
                "Enable Fluid Storage Cell",
                "Fluid Craft for AE2",
                true,
                "Enable this to generate the fluid storage cells. If you are playing with EC2, you can turn it off.");
        noFluidPacket = Config.getBoolean(
                "No Fluid Packet",
                "Fluid Craft for AE2",
                false,
                "Enable this to make normal ME Interface can emit fluid with fluid pattern, like the ME Dual Interface.");
        fluidIOBus = Config.getBoolean(
                "Enable AE2FC's Fluid I/O Bus",
                "Fluid Craft for AE2",
                true,
                "Enable this to add Fluid Import/Export Bus like EC2's one.");
        removeRecipe = Config.getBoolean(
                "Disable all recipes",
                "Fluid Craft for AE2",
                false,
                "Disable all recipes, for quick tweaker.");
        portableCellBattery = Config.get("Fluid Craft for AE2", "Portable Fluid Cell Battery Capacity", 20000D)
                .getDouble();
        packetSize = Config.get("Fluid Craft for AE2", "packetSize", 256, "Number of items to be sent per packet")
                .getInt();
        if (packetSize <= 0) packetSize = 256;
        packetRate = Config
                .get("Fluid Craft for AE2", "packetRate", 50, "Period at which packets are dispatched, in ms.")
                .getInt();
        if (packetRate <= 0) packetRate = 50;
        replaceEC2 = Config.getBoolean(
                "replaceEC2",
                "Fluid Craft for AE2",
                true,
                "Set true to handle missing item mappings from EC2. Note to work properly, you must have all relevant parts.");
        if (Config.hasChanged()) Config.save();
    }

    private static void loadCategory() {
        Config.addCustomCategoryComment("Fluid Craft for AE2", "Settings for AE2FC.");
    }
}
