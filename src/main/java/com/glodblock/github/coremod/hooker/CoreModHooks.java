package com.glodblock.github.coremod.hooker;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.IGuiTooltipHandler;
import appeng.util.InventoryAdaptor;

public class CoreModHooks {

    @Nullable
    public static InventoryAdaptor wrapInventory(@Nullable TileEntity tile, ForgeDirection face) {
        return tile != null ? FluidConvertingInventoryAdaptor.wrap(tile, face) : null;
    }

    public static long getCraftingByteCost(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop ? (long) Math.ceil(stack.getStackSize() / 1000D)
                : stack.getStackSize();
    }

    public static long getFluidDropsByteCost(long totalBytes, long originByte, IAEItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            return (long) Math.ceil(originByte / 1000D) + totalBytes;
        }
        return originByte + totalBytes;
    }

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

    public static boolean shouldShowTooltip(GuiContainer gui) {
        if (gui instanceof GuiFluidCraftConfirm guiCraftConfirm) {
            return guiCraftConfirm.getHoveredStack() == null;
        }
        return true;
    }
}
