package com.glodblock.github.crossmod.extracells.parts;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

public class ProxyStorageMonitor extends ProxyPart {

    public ProxyStorageMonitor(ProxyPartItem item) {
        super(item);
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Fluid
        int fluid = extra.getInteger("fluid");
        Fluid f = FluidRegistry.getFluid(fluid);
        NBTTagCompound itemDisplay = ProxyPart.createFluidNBT(f.getName(), 0L);
        extra.setTag("configuredItem", itemDisplay);
        extra.removeTag("amount");
        extra.removeTag("fluid");
        // Locked and other props
        extra.setBoolean("isLocked", extra.getBoolean("locked"));
        extra.removeTag("locked");
        extra.setByte("spin", (byte) 0);
        extra.setFloat("opacity", 255.0f);
        // Part
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");
        return extra;
    }

}
