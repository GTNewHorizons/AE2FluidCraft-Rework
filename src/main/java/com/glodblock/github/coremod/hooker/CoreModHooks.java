package com.glodblock.github.coremod.hooker;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.IGuiTooltipHandler;

public class CoreModHooks {

    public static ItemStack displayFluid(IAEItemStack aeStack) {
        if (aeStack.getItemStack() != null && aeStack.getItemStack().getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(aeStack.getItemStack());
            return ItemFluidPacket.newDisplayStack(fluid);
        } else return aeStack.getItemStack();
    }

    public static ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (gui instanceof IGuiTooltipHandler guiTooltipHandler) {
            return guiTooltipHandler.getHoveredStack();
        }
        return null;
    }
}
