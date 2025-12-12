package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.parts.reporting.PartStorageMonitor;

public class PartFluidStorageMonitor extends PartStorageMonitor {

    public PartFluidStorageMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        // Migrate old tag
        if (data.hasKey("configuredItem")) {
            final NBTTagCompound myItem = data.getCompoundTag("configuredItem");
            if (!myItem.hasNoTags() && !myItem.hasKey("StackType")) {
                myItem.setString("StackType", FLUID_STACK_TYPE.getId());
            }
        }

        super.readFromNBT(data);
    }
}
