package com.glodblock.github.crossmod.waila;

import java.text.NumberFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

public class Tooltip {

    public static String fluidFormat(String name, long amount) {
        return String.format("%s: %s mB", name, NumberFormat.getInstance().format(amount));
    }

    public static String fluidFormat(String name, long amount, long capacity) {
        return String.format(
                "%s: %s mB / %s mB",
                name,
                NumberFormat.getInstance().format(amount),
                NumberFormat.getInstance().format(capacity));
    }

    public static String partFluidBusFormat(int amount) {
        return String
                .format("%s: %s mB/t", I18n.format(NameConst.WAILA_SPEED), NumberFormat.getInstance().format(amount));
    }

    public static String tileFluidInterfaceFormat(String name, int amount, int face) {
        return String.format(
                "%s %s: %s mB",
                I18n.format(NameConst.GUI_FLUID_INTERFACE + "." + face),
                name,
                NumberFormat.getInstance().format(amount));
    }

    public static String tileLevelMaintainerFormat(String name, long quantity, long batch, boolean isEnable) {
        return String.format(
                "%s: %s / %s %s",
                name,
                NumberFormat.getInstance().format(quantity),
                NumberFormat.getInstance().format(batch),
                isEnable ? I18n.format(NameConst.WAILA_ENABLE) : I18n.format(NameConst.WAILA_DISABLE));
    }

    public static String partFluidTerminalFluidFormat(FluidStack fs) {
        int fid = Util.getFluidID(fs.getFluid());
        if (fid == -1) {
            return fs.getLocalizedName();
        } else if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            return String.format("%s (#%s) %s", fs.getLocalizedName(), fid, fid);
        } else {
            return String.format("%s %s", fs.getLocalizedName(), fid);
        }
    }
}
