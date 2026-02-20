package com.glodblock.github.util;

import cpw.mods.fml.common.Loader;

public final class ModAndClassUtil {

    /**
     * GTNH fork of GregTech5 Unofficial
     */
    public static boolean GT5NH = false;
    /**
     * GregTech5 / GregTech5 Unofficial (Blood-Asp's "official" GT5u)
     */
    public static boolean GT5 = false;
    public static boolean GT6 = false;
    public static boolean EC2 = false;
    public static boolean EIO = false;
    public static boolean FTR = false;
    public static boolean OC = false;
    public static boolean ThE = false;
    public static boolean WCT = false;
    public static boolean IC2 = false;
    public static boolean NEI = false;
    public static boolean GTPP = false;
    public static boolean WAILA = false;
    public static boolean WAILA_PLUGINS = false;
    public static boolean AVARITIA = false;
    public static boolean NEW_HORIZONS_CORE_MOD = false;
    public static boolean BAUBLES = false;

    @SuppressWarnings("all")
    public static void init() {

        if (Loader.isModLoaded("gregtech") && !Loader.isModLoaded("gregapi")) {
            try {
                Class.forName("gregtech.api.recipe.RecipeMap");
                GT5NH = true;
            } catch (ClassNotFoundException e) {
                GT5 = true;
            }
        }
        if (Loader.isModLoaded("gregapi") && Loader.isModLoaded("gregapi_post")) GT6 = true;
        if (Loader.isModLoaded("extracells")) EC2 = true;
        if (Loader.isModLoaded("EnderIO")) EIO = true;
        if (Loader.isModLoaded("Forestry")) FTR = true;
        if (Loader.isModLoaded("OpenComputers")) OC = true;
        if (Loader.isModLoaded("thaumicenergistics")) ThE = true;
        if (Loader.isModLoaded("ae2wct")) WCT = true;
        if (Loader.isModLoaded("IC2")) IC2 = true;
        if (Loader.isModLoaded("NotEnoughItems")) NEI = true;
        if (Loader.isModLoaded("miscutils")) GTPP = true;
        if (Loader.isModLoaded("Waila")) WAILA = true;
        if (Loader.isModLoaded("wailaplugins")) WAILA_PLUGINS = true;
        if (Loader.isModLoaded("Avaritia")) AVARITIA = true;
        if (Loader.isModLoaded("dreamcraft")) NEW_HORIZONS_CORE_MOD = true;
        if (Loader.isModLoaded("Baubles")) BAUBLES = true;
    }
}
