package com.glodblock.github.common.parts;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.util.Util;

import appeng.api.storage.StorageName;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.parts.misc.PartStorageBus;

public class PartFluidStorageBus extends PartStorageBus {

    public PartFluidStorageBus(ItemStack is) {
        super(is);
    }

    @Override
    public IAEStackType<?> getStackType() {
        return FLUID_STACK_TYPE;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        // Migrate from old data
        var config = this.getAEInventoryByName(StorageName.CONFIG);
        for (int i = 0; i < config.getSizeInventory(); i++) {
            IAEStack<?> stack = config.getAEStackInSlot(i);

            if (stack instanceof IAEItemStack ais) {
                IAEFluidStack afs = Util.getAEFluidFromItem(ais.getItemStack());
                config.putAEStackInSlot(i, afs);
            }
        }
    }
}
