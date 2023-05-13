package com.glodblock.github.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import codechicken.nei.api.IStackStringifyHandler;

public class FCStackStringifyHandler implements IStackStringifyHandler {

    @Override
    public NBTTagCompound convertItemStackToNBT(ItemStack stack, boolean saveStackSize) {
        return null;
    }

    @Override
    public ItemStack convertNBTToItemStack(NBTTagCompound nbtTag) {
        return null;
    }

    @Override
    public FluidStack getFluid(ItemStack item) {
        if (item.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(item);
            if (fluid == null) return null;
            fluid.amount /= Math.max(item.stackSize, 1);
            return fluid;
        } else if (item.getItem() instanceof ItemFluidPacket) {
            return ItemFluidPacket.getFluidStack(item);
        }
        return null;
    }
}
