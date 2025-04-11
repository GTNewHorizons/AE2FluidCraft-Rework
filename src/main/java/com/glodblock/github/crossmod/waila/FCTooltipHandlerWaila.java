package com.glodblock.github.crossmod.waila;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import mcp.mobius.waila.handlers.nei.TooltipHandlerWaila;

public class FCTooltipHandlerWaila extends TooltipHandlerWaila {

    @Override
    public List<String> handleItemDisplayName(GuiContainer arg0, ItemStack itemstack, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer arg0, ItemStack itemstack, int arg2, int arg3,
            List<String> currenttip) {
        if (itemstack != null && itemstack.getItem() instanceof ItemFluidDrop && currenttip.size() > 0) {
            FluidStack fs = ItemFluidDrop.getFluidStack(itemstack);
            if (fs == null) return currenttip;
            currenttip.set(0, Tooltip.partFluidTerminalFluidFormat(fs));
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                currenttip.set(
                        1,
                        EnumChatFormatting.GRAY
                                + String.format("%s:%s", Util.getFluidModID(fs.getFluid()), fs.getFluid().getName())
                                + EnumChatFormatting.RESET);
            }
            String modName = Util.getFluidModName(fs.getFluid());
            if (modName != null && !modName.equals("")) currenttip.set(currenttip.size() - 1, "§9§o" + modName);

            if (ModAndClassUtil.GT5NH || ModAndClassUtil.GT5) {
                ItemStack potion = Util.FluidUtil.getPotion(fs);
                if (potion != null && potion.getItem() instanceof ItemPotion) {
                    List<String> potionEffects = new ArrayList<>();
                    potion.getItem().addInformation(potion, Minecraft.getMinecraft().thePlayer, potionEffects, false);
                    currenttip.addAll(currenttip.size() - 1, potionEffects);
                }
            }
        }
        return currenttip;
    }

    @Override
    public List<String> handleTooltip(GuiContainer arg0, int arg1, int arg2, List<String> currenttip) {
        return currenttip;
    }
}
