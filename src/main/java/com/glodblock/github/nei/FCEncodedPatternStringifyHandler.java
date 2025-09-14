package com.glodblock.github.nei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.common.item.ItemFluidEncodedPattern;

import codechicken.nei.api.IStackStringifyHandler;
import codechicken.nei.recipe.stackinfo.DefaultStackStringifyHandler;
import codechicken.nei.recipe.stackinfo.GTFluidStackStringifyHandler;

public class FCEncodedPatternStringifyHandler implements IStackStringifyHandler {

    private static final DefaultStackStringifyHandler defaultStackStringifyHandler = new DefaultStackStringifyHandler();
    private static final GTFluidStackStringifyHandler gtFluidStackStringifyHandler = new GTFluidStackStringifyHandler();

    public NBTTagCompound convertItemStackToNBT(ItemStack stack, boolean saveStackSize) {
        if (!(stack.getItem() instanceof ItemFluidEncodedPattern pattern)) {
            return null;
        }

        stack = pattern.getOutput(stack);

        if (stack == null) {
            return null;
        }

        NBTTagCompound fluidNbtTag = gtFluidStackStringifyHandler.convertItemStackToNBT(stack, saveStackSize);

        if (fluidNbtTag != null) {
            return fluidNbtTag;
        }

        return defaultStackStringifyHandler.convertItemStackToNBT(stack, saveStackSize);
    }
}
