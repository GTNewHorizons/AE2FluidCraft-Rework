package com.glodblock.github.crossmod.extracells.parts;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.glodblock.github.crossmod.extracells.ProxyPart;
import com.glodblock.github.crossmod.extracells.ProxyPartItem;

public class ProxyFluidInterface extends ProxyPart {

    public ProxyFluidInterface(ProxyPartItem item) {
        super(item);
    }

    @Nonnull
    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        // Internal fluids - ConfigInv and FluidInv
        NBTTagCompound tank = extra.getCompoundTag("tank");
        NBTTagCompound configTank = new NBTTagCompound();
        configTank.setTag("#0", ProxyPart.createFluidDisplayTag(tank.getString("FluidName")));
        extra.setTag("ConfigInv", configTank);
        NBTTagCompound fluidTank = new NBTTagCompound();
        NBTTagCompound fluid = ProxyPart.createFluidNBT(tank.getString("FluidName"), tank.getLong("Amount"));
        fluidTank.setTag("#0", fluid);
        extra.setTag("FluidInv", fluidTank);
        extra.removeTag("tank");
        // Patterns
        NBTTagList patterns = extra.getCompoundTag("inventory").getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        NBTTagCompound newPatterns = new NBTTagCompound();
        for (int i = 0; i < patterns.tagCount(); ++i) {
            NBTTagCompound p = patterns.getCompoundTagAt(i);
            p.removeTag("Slot");
            newPatterns.setTag("#" + i, p);
        }
        extra.removeTag("inventory");
        extra.setTag("patterns", newPatterns);
        // Part
        extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
        extra.removeTag("node");
        return extra;
    }

}
