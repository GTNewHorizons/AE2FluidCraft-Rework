package com.glodblock.github.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.AEInventoryCrafting;

public class FluidConvertingInventoryCrafting extends AEInventoryCrafting {

    public FluidConvertingInventoryCrafting(Container container, int width, int height) {
        super(container, width, height);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(stack);
            if (fluid != null) {
                super.setInventorySlotContents(index, ItemFluidPacket.newStack(new FluidStack(fluid, stack.stackSize)));
            } else {
                super.setInventorySlotContents(
                        index,
                        ItemFluidPacket.newStack(new FluidStack(FluidRegistry.WATER, 1000)));
            }
        } else {
            super.setInventorySlotContents(index, stack);
        }
    }

    @Override
    public void setInventorySlotContents(int index, IAEItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemFluidDrop) {
            IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(stack);
            if (fluid != null) {
                super.setInventorySlotContents(index, ItemFluidPacket.newStack(fluid));
            } else {
                super.setInventorySlotContents(
                        index,
                        ItemFluidPacket.newStack(new FluidStack(FluidRegistry.WATER, 1000)));
            }
        } else {
            super.setInventorySlotContents(index, stack);
        }
    }
}
